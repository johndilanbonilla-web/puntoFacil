package com.puntofacil.puntofacilbackend.controller;

import com.puntofacil.puntofacilbackend.service.ReporteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private TemplateEngine templateEngine;

    // ================================
    // MÉTODOS AUXILIARES PARA SESIÓN
    // ================================
    private Integer getEmpresaId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idEmpresa");
        return (id != null) ? id : 1; // 1 por defecto por seguridad
    }

    private Integer getSucursalId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("idSucursal");
        return (id != null) ? id : 1; // 1 por defecto por seguridad
    }

    // ==========================================================
    // 1. REPORTE: VENTAS POR PRODUCTO
    // ==========================================================

    @GetMapping("/ventas/producto")
    public String ventasProducto(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            Model model,
            HttpSession session) {

        if (inicio == null) inicio = LocalDate.now().minusDays(7);
        if (fin == null) fin = LocalDate.now();

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasPorProducto(inicio, fin, idEmpresa, idSucursal);

        double totalVentas = data.stream().mapToDouble(r -> ((Number) r.get("total_ventas")).doubleValue()).sum();
        int totalCantidad = data.stream().mapToInt(r -> ((Number) r.get("total_vendido")).intValue()).sum();

        model.addAttribute("data", data);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalCantidad", totalCantidad);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);

        return "reportes/ventas/ventas-producto";
    }

    @GetMapping("/ventas/producto/pdf")
    public void exportarPdf(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            HttpServletResponse response,
            HttpSession session) throws Exception {

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasPorProducto(inicio, fin, idEmpresa, idSucursal);
        double totalVentas = data.stream().mapToDouble(r -> ((Number) r.get("total_ventas")).doubleValue()).sum();

        Context context = new Context();
        context.setVariable("data", data);
        context.setVariable("inicio", inicio);
        context.setVariable("fin", fin);
        context.setVariable("totalVentas", totalVentas);

        String htmlContent = templateEngine.process("reportes/ventas/ventas-producto-pdf", context);

        Document doc = Jsoup.parse(htmlContent, "UTF-8");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String xhtml = doc.html();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Ventas_" + LocalDate.now() + ".pdf");

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(xhtml, "/");
        builder.toStream(response.getOutputStream());
        builder.run();
    }

    @GetMapping("/ventas/producto/excel")
    public void exportarExcel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            HttpServletResponse response,
            HttpSession session) throws Exception {

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasPorProducto(inicio, fin, idEmpresa, idSucursal);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ventas por Producto");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        String[] columnas = {"Fecha", "Tipo Documento", "N° Documento", "Producto", "Cantidad", "Total Ventas ($)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Map<String, Object> item : data) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(String.valueOf(item.get("fecha_venta")));
            row.createCell(1).setCellValue(String.valueOf(item.get("tipo_documento")));
            row.createCell(2).setCellValue(String.valueOf(item.get("numero_documento")));
            row.createCell(3).setCellValue(String.valueOf(item.get("producto")));
            row.createCell(4).setCellValue(((Number) item.get("total_vendido")).doubleValue());
            row.createCell(5).setCellValue(((Number) item.get("total_ventas")).doubleValue());
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Ventas_" + LocalDate.now() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    // ==========================================================
    // 🖨️ VISTA PREVIA DE IMPRESIÓN (VENTAS POR PRODUCTO)
    // ==========================================================
    @GetMapping("/ventas/producto/vista")
    public String vistaImpresionProducto(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            Model model, HttpSession session) {

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasPorProducto(inicio, fin, idEmpresa, idSucursal);

        double totalVentas = data.stream().mapToDouble(r -> ((Number) r.get("total_ventas")).doubleValue()).sum();

        model.addAttribute("data", data);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("totalVentas", totalVentas);

        return "reportes/ventas/ventas-producto-vista";
    }

    // ==========================================================
    // 2. REPORTE: VENTAS MENSUALES (WEB, PDF, EXCEL)
    // ==========================================================

    @GetMapping("/ventas/mes")
    public String ventasMensual(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            Model model, HttpSession session) {

        if (inicio == null) inicio = LocalDate.now().withDayOfYear(1);
        if (fin == null) fin = LocalDate.now();

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> dataDetalle = reporteService.ventasDetalleMes(inicio, fin, idEmpresa, idSucursal);
        List<Map<String, Object>> dataGrafica = reporteService.ventasAgrupadasMes(inicio, fin, idEmpresa, idSucursal);

        double totalVentas = dataDetalle.stream().mapToDouble(r -> ((Number) r.get("total_ventas")).doubleValue()).sum();

        model.addAttribute("dataDetalle", dataDetalle);
        model.addAttribute("dataGrafica", dataGrafica);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);

        return "reportes/ventas/ventas-mes";
    }

    @GetMapping("/ventas/mes/pdf")
    public void exportarPdfMes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            HttpServletResponse response,
            HttpSession session) throws Exception {

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasDetalleMes(inicio, fin, idEmpresa, idSucursal);

        double totalVentas = data.stream().mapToDouble(r -> ((Number) r.get("total_ventas")).doubleValue()).sum();
        double totalVendido = data.stream().mapToDouble(r -> ((Number) r.get("total_vendido")).doubleValue()).sum();

        Context context = new Context();
        context.setVariable("data", data);
        context.setVariable("inicio", inicio);
        context.setVariable("fin", fin);
        context.setVariable("totalVentas", totalVentas);
        context.setVariable("totalVendido", totalVendido);

        String htmlContent = templateEngine.process("reportes/ventas/ventas-mes-pdf", context);

        Document doc = Jsoup.parse(htmlContent, "UTF-8");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String xhtml = doc.html();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Mensual_" + LocalDate.now() + ".pdf");

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(xhtml, "/");
        builder.toStream(response.getOutputStream());
        builder.run();
    }

    @GetMapping("/ventas/mes/excel")
    public void exportarExcelMes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            HttpServletResponse response,
            HttpSession session) throws Exception {

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasDetalleMes(inicio, fin, idEmpresa, idSucursal);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte Mensual");

        // --- ESTILOS ---
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        CellStyle negativeStyle = workbook.createCellStyle();
        Font negativeFont = workbook.createFont();
        negativeFont.setColor(IndexedColors.RED.getIndex());
        negativeStyle.setFont(negativeFont);

        // --- CONSTRUYENDO EL EXCEL ---
        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("PUNTO FÁCIL - Sistema de Gestión POS");
        Cell titleCell = row0.createCell(4);
        titleCell.setCellValue("REPORTE DE VENTAS MENSUAL");
        titleCell.setCellStyle(titleStyle);

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("RUC: 123456789 | Av. Principal 1234");
        row1.createCell(4).setCellValue("Período: " + inicio + " al " + fin);

        sheet.createRow(3); // Espacio

        String[] columnas = {"FECHA", "N° DOCUMENTO", "CLIENTE", "TIPO DOC.", "FORMA PAGO", "CANTIDAD", "TOTAL VENTAS ($)"};
        Row headerRow = sheet.createRow(4);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 5;
        double sumVentas = 0;
        for (Map<String, Object> item : data) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(String.valueOf(item.get("fecha_venta")));
            row.createCell(1).setCellValue(String.valueOf(item.get("numero_documento")));
            row.createCell(2).setCellValue(String.valueOf(item.get("cliente")));
            row.createCell(3).setCellValue(String.valueOf(item.get("tipo_doc")));
            row.createCell(4).setCellValue(String.valueOf(item.get("forma_pago")));

            double cant = ((Number) item.get("total_vendido")).doubleValue();
            double total = ((Number) item.get("total_ventas")).doubleValue();
            sumVentas += total;

            Cell cellCant = row.createCell(5);
            cellCant.setCellValue(cant);
            if (cant < 0) cellCant.setCellStyle(negativeStyle);

            Cell cellTotal = row.createCell(6);
            cellTotal.setCellValue(total);
            if (total < 0) cellTotal.setCellStyle(negativeStyle);
        }

        Row totalRow = sheet.createRow(rowIdx + 1);
        Cell totalLabel = totalRow.createCell(5);
        totalLabel.setCellValue("TOTAL GENERAL:");
        totalLabel.setCellStyle(headerStyle);

        Cell totalValue = totalRow.createCell(6);
        totalValue.setCellValue(sumVentas);
        totalValue.setCellStyle(headerStyle);

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Mensual_" + LocalDate.now() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ==========================================================
    // 🖨️ VISTA PREVIA DE IMPRESIÓN CORPORATIVA (VENTAS POR MES)
    // ==========================================================
    @GetMapping("/ventas/mes/vista")
    public String vistaImpresionMes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            Model model, HttpSession session) {

        Integer idEmpresa = getEmpresaId(session);
        Integer idSucursal = getSucursalId(session);

        List<Map<String, Object>> data = reporteService.ventasDetalleMes(inicio, fin, idEmpresa, idSucursal);

        double totalVentas = data.stream().mapToDouble(r -> ((Number) r.get("total_ventas")).doubleValue()).sum();
        double totalVendido = data.stream().mapToDouble(r -> ((Number) r.get("total_vendido")).doubleValue()).sum();

        model.addAttribute("data", data);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalVendido", totalVendido);

        // Retorna un HTML sin el layout oscuro, solo el diseño de papel
        return "reportes/ventas/ventas-mes-vista";
    }

    // ==========================================================
    // OTROS REPORTES PENDIENTES
    // ==========================================================

    @GetMapping("/ventas/forma-pago")
    public String ventasFormaPago() { return "reportes/ventas/ventas-forma-pago"; }

    @GetMapping("/gastos")
    public String gastos() { return "reportes/gastos/gastos"; }

    @GetMapping("/inventario")
    public String inventario() { return "reportes/inventarios/inventario"; }
}