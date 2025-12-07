<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="statusDraft" select="'Koncept'"/>
    <xsl:variable name="statusSent" select="'Odesláno'"/>
    <xsl:variable name="statusPending" select="'Čeká na platbu'"/>
    <xsl:variable name="statusPaid" select="'Zaplaceno'"/>
    <xsl:variable name="statusCancelled" select="'Stornováno'"/>
    <xsl:variable name="statusOverdue" select="'Po splatnosti'"/>
    <xsl:variable name="statusPaidOverdue" select="'Zaplaceno po splatnosti'"/>
    <xsl:template match="/ReportWithRequest">
        <html>
            <head>
                <title>Souhrnný report faktur</title>
                <style type="text/css">
                    body { font-family: DejaVu Sans, Arial, sans-serif; font-size: 12px; }
                    h1 { text-align: center; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 20px; font-size: 11px; }
                    th, td { border: 1px solid #333; padding: 2px 6px; text-align: left; }
                    th { background: #eee; }
                    .graf { margin: 20px 0; }
                </style>
            </head>
            <body>
                <h1>Souhrnný report faktur</h1>
                <h2>Parametry požadavku</h2>
                <table>
                    <xsl:if test="InvoiceReportRequest/customerId &gt; 0">
                        <tr><th>ID zákazníka</th><td><xsl:value-of select="InvoiceReportRequest/customerId"/></td></tr>
                    </xsl:if>
                    <xsl:if test="string-length(InvoiceReportRequest/invoiceNumber) &gt; 0">
                        <tr><th>Číslo faktury</th><td><xsl:value-of select="InvoiceReportRequest/invoiceNumber"/></td></tr>
                    </xsl:if>
                    <xsl:if test="string-length(InvoiceReportRequest/referenceNumber) &gt; 0">
                        <tr><th>Referenční číslo</th><td><xsl:value-of select="InvoiceReportRequest/referenceNumber"/></td></tr>
                    </xsl:if>
                    <xsl:if test="count(InvoiceReportRequest/issueDateFrom) &gt; 0">
                        <tr><th>Datum vystavení od</th>
                            <td>
                                <xsl:call-template name="formatDate">
                                    <xsl:with-param name="dateNodes" select="InvoiceReportRequest/issueDateFrom"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:if test="count(InvoiceReportRequest/issueDateTo) &gt; 0">
                        <tr><th>Datum vystavení do</th>
                            <td>
                                <xsl:call-template name="formatDate">
                                    <xsl:with-param name="dateNodes" select="InvoiceReportRequest/issueDateTo"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:if test="count(InvoiceReportRequest/dueDateFrom) &gt; 0">
                        <tr><th>Datum splatnosti od</th>
                            <td>
                                <xsl:call-template name="formatDate">
                                    <xsl:with-param name="dateNodes" select="InvoiceReportRequest/dueDateFrom"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:if test="count(InvoiceReportRequest/dueDateTo) &gt; 0">
                        <tr><th>Datum splatnosti do</th>
                            <td>
                                <xsl:call-template name="formatDate">
                                    <xsl:with-param name="dateNodes" select="InvoiceReportRequest/dueDateTo"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:if test="string-length(InvoiceReportRequest/minAmount) &gt; 0">
                        <tr><th>Minimální částka</th><td><xsl:value-of select="InvoiceReportRequest/minAmount"/></td></tr>
                    </xsl:if>
                    <xsl:if test="string-length(InvoiceReportRequest/maxAmount) &gt; 0">
                        <tr><th>Maximální částka</th><td><xsl:value-of select="InvoiceReportRequest/maxAmount"/></td></tr>
                    </xsl:if>
                    <xsl:if test="string-length(InvoiceReportRequest/currency) &gt; 0">
                        <tr><th>Měna</th><td><xsl:value-of select="InvoiceReportRequest/currency"/></td></tr>
                    </xsl:if>
                    <xsl:if test="string-length(InvoiceReportRequest/status) &gt; 0">
                        <tr><th>Stav faktury</th><td><xsl:value-of select="InvoiceReportRequest/status"/></td></tr>
                    </xsl:if>
                </table>
                <h2>Výsledky reportu</h2>
                <table>
                    <tr>
                        <th>Datum generování</th>
                        <th>Celkový počet faktur</th>
                        <th>Celková částka</th>
                    </tr>
                    <tr>
                        <td>
                            <xsl:call-template name="formatGeneratedAt">
                                <xsl:with-param name="dateNodes" select="AggregatedReportResponse/generatedAt"/>
                            </xsl:call-template>
                        </td>
                        <td><xsl:value-of select="AggregatedReportResponse/totalInvoices"/></td>
                        <td><xsl:value-of select="AggregatedReportResponse/totalAmount"/></td>
                    </tr>
                </table>
                <xsl:if test="AggregatedReportResponse/limitReached='true'">
                    <p style="color:red"><b>Pozor: dosažen maximální limit faktur v reportu!</b></p>
                </xsl:if>
                <h3 style="page-break-before: always;">Souhrn podle zákazníka</h3>
                <table>
                    <tr>
                        <th>Zákazník</th>
                        <th>Počet</th>
                        <th>Částka</th>
                    </tr>
                    <xsl:for-each select="AggregatedReportResponse/perCustomer/perCustomer">
                        <tr>
                            <td><xsl:value-of select="customerName"/></td>
                            <td><xsl:value-of select="invoiceCount"/></td>
                            <td><xsl:value-of select="concat(totalAmount, ' ', ../../../../totalAmount/../currency)"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
                <h3>Seznam faktur</h3>
                <table style="width:100%;border-collapse:collapse;">
                    <tr style="background-color:#F3F5F7;">
                        <th>Číslo</th>
                        <th>Referenční číslo</th>
                        <th>Zákazník</th>
                        <th>Dodavatel</th>
                        <th>Datum vystavení</th>
                        <th>Datum splatnosti</th>
                        <th>Částka</th>
                        <th>Stav</th>
                    </tr>
                    <xsl:for-each select="AggregatedReportResponse/invoices/invoices">
                        <tr>
                            <td><xsl:value-of select="invoiceNumber"/></td>
                            <td><xsl:value-of select="referenceNumber"/></td>
                            <td><xsl:value-of select="customerName"/></td>
                            <td><xsl:value-of select="supplierName"/></td>
                            <td>
                                <xsl:call-template name="formatDate">
                                    <xsl:with-param name="dateNodes" select="issueDate"/>
                                </xsl:call-template>
                            </td>
                            <td>
                                <xsl:call-template name="formatDate">
                                    <xsl:with-param name="dateNodes" select="dueDate"/>
                                </xsl:call-template>
                            </td>
                            <td><xsl:value-of select="concat(amount, ' ', currency)"/></td>
                            <td>
                                <xsl:call-template name="translateStatus">
                                    <xsl:with-param name="status" select="status"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="formatDate">
        <xsl:param name="dateNodes"/>
        <xsl:variable name="year" select="$dateNodes[1]"/>
        <xsl:variable name="month" select="$dateNodes[2]"/>
        <xsl:variable name="day" select="$dateNodes[3]"/>
        <xsl:choose>
            <xsl:when test="$year and $month and $day">
                <xsl:value-of select="concat($year, '-', format-number($month, '00'), '-', format-number($day, '00'))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$year"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="formatGeneratedAt">
        <xsl:param name="dateNodes"/>
        <xsl:variable name="year" select="$dateNodes[1]"/>
        <xsl:variable name="month" select="$dateNodes[2]"/>
        <xsl:variable name="day" select="$dateNodes[3]"/>
        <xsl:variable name="hour" select="$dateNodes[4]"/>
        <xsl:variable name="minute" select="$dateNodes[5]"/>
        <xsl:variable name="second" select="$dateNodes[6]"/>
        <xsl:choose>
            <xsl:when test="$year and $month and $day and $hour and $minute and $second">
                <xsl:value-of select="concat($year, '-', format-number($month, '00'), '-', format-number($day, '00'), ' ', format-number($hour, '00'), ':', format-number($minute, '00'), ':', format-number($second, '00'))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$year"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="translateStatus">
        <xsl:param name="status"/>
        <xsl:choose>
            <xsl:when test="$status='DRAFT'">
                <xsl:value-of select="$statusDraft"/>
            </xsl:when>
            <xsl:when test="$status='SENT'">
                <xsl:value-of select="$statusSent"/>
            </xsl:when>
            <xsl:when test="$status='PENDING'">
                <xsl:value-of select="$statusPending"/>
            </xsl:when>
            <xsl:when test="$status='PAID'">
                <xsl:value-of select="$statusPaid"/>
            </xsl:when>
            <xsl:when test="$status='CANCELLED'">
                <xsl:value-of select="$statusCancelled"/>
            </xsl:when>
            <xsl:when test="$status='OVERDUE'">
                <xsl:value-of select="$statusOverdue"/>
            </xsl:when>
            <xsl:when test="$status='PAID_OVERDUE'">
                <xsl:value-of select="$statusPaidOverdue"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$status"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
