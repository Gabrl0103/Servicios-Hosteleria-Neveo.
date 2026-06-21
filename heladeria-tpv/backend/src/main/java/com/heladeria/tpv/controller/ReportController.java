package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.PaymentMethodReportResponse;
import com.heladeria.tpv.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Ejemplo: /api/reports/by-payment-method?from=2026-06-01&to=2026-06-17
     * Funciona sin importar si la caja esta abierta o cerrada.
     */
    @GetMapping("/by-payment-method")
    public PaymentMethodReportResponse getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.getReportByDateRange(from, to);
    }

    /**
     * Resumen completo del periodo: KPIs, comparacion vs periodo anterior,
     * ventas por dia para el grafico, ranking de productos y metodos de pago.
     */
    @GetMapping("/summary")
    public com.heladeria.tpv.dto.ReportSummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.getReportSummary(from, to);
    }

    /**
     * Desglose en vivo del turno actual, para la barra fija de la pantalla de ventas.
     */
    @GetMapping("/current-shift")
    public PaymentMethodReportResponse getCurrentShift() {
        return reportService.getCurrentShiftReport();
    }

    /**
     * Resumen de un turno especifico (para el cierre de caja).
     */
    @GetMapping("/shift/{cashRegisterId}")
    public PaymentMethodReportResponse getShiftReport(@PathVariable Long cashRegisterId) {
        return reportService.getShiftReport(cashRegisterId);
    }

    /**
     * Comprobante detallado de un turno (fechas, ventas confirmadas/anuladas,
     * desglose por metodo de pago).
     */
    @GetMapping("/shift/{cashRegisterId}/receipt")
    public com.heladeria.tpv.dto.ShiftReceiptResponse getShiftReceipt(@PathVariable Long cashRegisterId) {
        return reportService.getShiftReceipt(cashRegisterId);
    }

    @GetMapping("/shift/{cashRegisterId}/expected-cash")
    public com.heladeria.tpv.dto.ExpectedCashResponse getExpectedCash(@PathVariable Long cashRegisterId) {
        return reportService.getExpectedCash(cashRegisterId);
    }

    /**
     * Los 4 KPIs fijos del dashboard: cuadre actual, ultimos 7 dias,
     * ultimos 30 dias, y total del año.
     */
    @GetMapping("/dashboard-kpis")
    public com.heladeria.tpv.dto.DashboardKpisResponse getDashboardKpis() {
        return reportService.getDashboardKpis();
    }

    /**
     * Grafico de barras: total facturado por mes, ultimos 6 meses.
     */
    @GetMapping("/monthly-bar-chart")
    public java.util.List<com.heladeria.tpv.dto.BarChartPoint> getMonthlyBarChart() {
        return reportService.getMonthlyBarChart();
    }
}
