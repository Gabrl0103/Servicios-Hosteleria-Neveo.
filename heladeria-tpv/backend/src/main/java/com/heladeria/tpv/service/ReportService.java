package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.*;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.*;
import com.heladeria.tpv.repository.CashRegisterRepository;
import com.heladeria.tpv.repository.ExpenseRepository;
import com.heladeria.tpv.repository.OrderItemRepository;
import com.heladeria.tpv.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CashRegisterService cashRegisterService;
    private final CashRegisterRepository cashRegisterRepository;
    private final BusinessSettingsService businessSettingsService;
    private final ExpenseRepository expenseRepository;

    public ReportService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          CashRegisterService cashRegisterService,
                          CashRegisterRepository cashRegisterRepository,
                          BusinessSettingsService businessSettingsService,
                          ExpenseRepository expenseRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cashRegisterService = cashRegisterService;
        this.cashRegisterRepository = cashRegisterRepository;
        this.businessSettingsService = businessSettingsService;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Reporte de ventas por metodo de pago en un rango de fechas (inclusivo).
     * No depende de si la caja esta abierta o cerrada.
     */
    public PaymentMethodReportResponse getReportByDateRange(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        var rows = orderRepository.sumTotalsByPaymentMethodBetween(fromDateTime, toDateTime);
        return new PaymentMethodReportResponse(rows);
    }

    /**
     * Resumen completo del periodo: KPIs, comparacion vs periodo anterior
     * equivalente, ventas por dia (para el grafico), ranking de productos
     * mas vendidos, y desglose por metodo de pago. Todo calculado en
     * memoria a partir de las ordenes confirmadas del rango, sin depender
     * de funciones SQL especificas de un motor de base de datos.
     */
    public ReportSummaryResponse getReportSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findConfirmedBetween(fromDateTime, toDateTime);

        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = orders.size();

        BigDecimal averageTicket = totalOrders > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Periodo anterior equivalente: mismo numero de dias, justo antes del rango.
        long daysInRange = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousFrom = from.minusDays(daysInRange);
        LocalDate previousTo = from.minusDays(1);
        LocalDateTime previousFromDateTime = previousFrom.atStartOfDay();
        LocalDateTime previousToDateTime = previousTo.plusDays(1).atStartOfDay();

        List<Order> previousOrders = orderRepository.findConfirmedBetween(previousFromDateTime, previousToDateTime);
        BigDecimal previousAmount = previousOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double percentChange = null;
        if (previousAmount.compareTo(BigDecimal.ZERO) > 0) {
            percentChange = totalAmount.subtract(previousAmount)
                    .divide(previousAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // Ventas por dia, para el grafico de linea.
        Map<LocalDate, List<Order>> ordersByDay = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<DailySalesPoint> dailySales = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            List<Order> dayOrders = ordersByDay.getOrDefault(day, List.of());
            BigDecimal dayTotal = dayOrders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            dailySales.add(new DailySalesPoint(day, dayTotal, dayOrders.size()));
        }

        DailySalesPoint bestDay = dailySales.stream()
                .max(Comparator.comparing(DailySalesPoint::getTotal))
                .filter(d -> d.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .orElse(null);

        // Ranking de productos mas vendidos.
        List<OrderItem> items = orderItemRepository.findConfirmedItemsBetween(fromDateTime, toDateTime);
        Map<Long, ProductAccumulator> byProduct = new LinkedHashMap<>();
        for (OrderItem item : items) {
            Long productId = item.getProduct().getId();
            ProductAccumulator acc = byProduct.computeIfAbsent(productId, id -> new ProductAccumulator(
                    item.getProduct().getName(), item.getProduct().getCategory()));
            acc.quantity += item.getQuantity();
            acc.amount = acc.amount.add(item.getSubtotal());
        }

        List<ProductRankingItem> topProducts = byProduct.entrySet().stream()
                .map(e -> new ProductRankingItem(e.getKey(), e.getValue().name, e.getValue().category,
                        e.getValue().quantity, e.getValue().amount))
                .sorted(Comparator.comparing(ProductRankingItem::getTotalAmount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        PaymentMethodReportResponse paymentBreakdown =
                new PaymentMethodReportResponse(orderRepository.sumTotalsByPaymentMethodBetween(fromDateTime, toDateTime));

        return new ReportSummaryResponse(
                totalAmount, totalOrders, previousAmount, percentChange,
                averageTicket, bestDay, dailySales, topProducts, paymentBreakdown);
    }

    private static class ProductAccumulator {
        final String name;
        final String category;
        long quantity = 0;
        BigDecimal amount = BigDecimal.ZERO;

        ProductAccumulator(String name, String category) {
            this.name = name;
            this.category = category;
        }
    }

    /**
     * Desglose en vivo del turno actualmente abierto, por metodo de pago.
     * Util para la barra fija que el cajero ve mientras vende.
     */
    public PaymentMethodReportResponse getCurrentShiftReport() {
        CashRegister openRegister = cashRegisterService.getOpenRegisterOrThrow();
        var rows = orderRepository.sumTotalsByPaymentMethodForCashRegister(openRegister.getId());
        return new PaymentMethodReportResponse(rows);
    }

    /**
     * Resumen final de un turno especifico, ya cerrado o no.
     */
    public PaymentMethodReportResponse getShiftReport(Long cashRegisterId) {
        var rows = orderRepository.sumTotalsByPaymentMethodForCashRegister(cashRegisterId);
        return new PaymentMethodReportResponse(rows);
    }

    /**
     * Comprobante detallado de un turno, con fechas, total de ventas
     * confirmadas y anuladas, y el desglose por metodo de pago.
     * Equivalente al "comprobante de informe diario" pero solo con
     * los datos que este sistema realmente maneja.
     */
    public ShiftReceiptResponse getShiftReceipt(Long cashRegisterId) {
        CashRegister register = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        PaymentMethodReportResponse breakdown = getShiftReport(cashRegisterId);

        long confirmedCount = orderRepository.countByCashRegisterIdAndStatus(
                cashRegisterId, OrderStatus.CONFIRMADO);
        long voidedCount = orderRepository.countByCashRegisterIdAndStatus(
                cashRegisterId, OrderStatus.ANULADO);

        BusinessSettings bs = businessSettingsService.get();

        BigDecimal openingAmount = register.getOpeningAmount() != null ? register.getOpeningAmount() : BigDecimal.ZERO;
        BigDecimal cashSales = breakdown.getBreakdown().get(PaymentMethod.EFECTIVO).getTotal();

        List<Expense> expenseEntities = expenseRepository.findByCashRegisterId(cashRegisterId);
        BigDecimal totalExpenses = expenseEntities.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ExpenseResponse> expenses = expenseEntities.stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getDescription(), e.getAmount(), e.getCreatedAt()))
                .collect(Collectors.toList());

        BigDecimal expectedCash = openingAmount.add(cashSales).subtract(totalExpenses);

        return new ShiftReceiptResponse(
                register.getId(),
                register.getOpenedAt(),
                register.getClosedAt(),
                confirmedCount,
                voidedCount,
                register.getCashierName(),
                bs.getBusinessName(),
                bs.getNit(),
                bs.getAddress(),
                bs.getPhone(),
                bs.getLogoBase64(),
                breakdown,
                openingAmount,
                cashSales,
                totalExpenses,
                expenses,
                expectedCash);
    }

    public ExpectedCashResponse getExpectedCash(Long cashRegisterId) {
        CashRegister register = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        BigDecimal openingAmount = register.getOpeningAmount() != null ? register.getOpeningAmount() : BigDecimal.ZERO;
        PaymentMethodReportResponse breakdown = getShiftReport(cashRegisterId);
        BigDecimal cashSales = breakdown.getBreakdown().get(PaymentMethod.EFECTIVO).getTotal();

        BigDecimal totalExpenses = expenseRepository.findByCashRegisterId(cashRegisterId).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedCash = openingAmount.add(cashSales).subtract(totalExpenses);

        return new ExpectedCashResponse(openingAmount, cashSales, totalExpenses, expectedCash);
    }

    /**
     * Los 4 KPIs fijos del dashboard de reportes: cuadre actual,
     * ultimos 7 dias, ultimos 30 dias, y total del año en curso.
     * Si no hay turno abierto, el cuadre actual queda en cero.
     */
    public DashboardKpisResponse getDashboardKpis() {
        LocalDate today = LocalDate.now();

        BigDecimal currentShiftTotal = BigDecimal.ZERO;
        try {
            CashRegister openRegister = cashRegisterService.getOpenRegisterOrThrow();
            currentShiftTotal = sumTotals(orderRepository.sumTotalsByPaymentMethodForCashRegister(openRegister.getId()));
        } catch (Exception ignored) {
            // No hay turno abierto; el KPI queda en cero, no es un error real.
        }

        BigDecimal last7 = sumOrdersBetween(today.minusDays(6), today);
        BigDecimal last30 = sumOrdersBetween(today.minusDays(29), today);
        BigDecimal yearTotal = sumOrdersBetween(today.withDayOfYear(1), today);

        return new DashboardKpisResponse(currentShiftTotal, last7, last30, yearTotal);
    }

    private BigDecimal sumTotals(List<OrderRepository.PaymentMethodTotalsProjection> rows) {
        return rows.stream().map(OrderRepository.PaymentMethodTotalsProjection::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumOrdersBetween(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();
        return orderRepository.findConfirmedBetween(fromDateTime, toDateTime).stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Grafico de barras: total facturado por mes, de los ultimos 6 meses
     * (incluyendo el actual). Complementa al grafico de linea diaria,
     * dando una vista de tendencia mas larga.
     */
    public List<BarChartPoint> getMonthlyBarChart() {
        LocalDate today = LocalDate.now();
        List<BarChartPoint> points = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
            BigDecimal monthTotal = sumOrdersBetween(monthDate, monthEnd);
            String label = monthDate.getMonth().getDisplayName(
                    java.time.format.TextStyle.SHORT, new Locale("es", "ES"));
            points.add(new BarChartPoint(label, monthTotal));
        }

        return points;
    }
}
