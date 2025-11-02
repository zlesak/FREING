<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
                xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
                xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100">
    <!-- An XSLT 2.0 identity transform - copies the original xml to ouput -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:template match="/">
        <html lang="cs"><!--Attribute 'type' must appear on element 'style'.   xmlns="http://www.w3.org/1999/xhtml"-->
            <head>
                <meta content='text/html; charset=UTF-8'/>
                <style>
                    body {
                    font-family: DejaVu Sans, sans-serif;
                    font-size: 12pt;
                    }
                    .at tr td:nth-child(even){
                        text-align:right;
                    }
                    address{
                        margin: 10px;
                    }
                    .box:nth-child(even){
                        background-color: #F3F5F7;
                    }
                    .box{
                        margin:10px;
                    }
                </style>
            </head>
            <body>
                <h2 style="text-align:right">Faktura číslo: <xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:ID"/></h2>
                <xsl:variable name="cur" select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceCurrencyCode"/>

                <div style="display:table;width:100%">
                <div style="display:table-row">
                    <div style="display:table-cell;width:50%;height:300px">
                        <xsl:variable name="logo" select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:IncludedNote/ram:Content[position() = 1 or position() = 2]"/>

                        <img id="logoImg" src="{concat('data:image/png;base64,',$logo[2])}" style="height:260px;width:260px"/>
                    </div>
                    <div style="display:table-cell">
                        <table style="width:100%">
                            <tr>
                                <xsl:variable name="qr" select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:IncludedNote/ram:Content[position() = 1]"/>

                                <img id="qrImg" src="{concat('data:image/png;base64,',$qr[1])}" style="height:260px;width:260px"/>
                            </tr>
                            <tr>
                                <td>Datum vytvoření: </td>
                                <xsl:variable name="date" select="/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:IssueDateTime/udt:DateTimeString"/>
                                <xsl:variable name="year" select="substring($date,1,4)"/>
                                <xsl:variable name="month" select="substring($date,5,2)"/>
                                <xsl:variable name="day" select="substring($date,7,2)"/>
                                <td><xsl:value-of select="concat($day, '.', $month, '.', $year)"/></td>
                            </tr>
                            <tr>
                                <td>Datum doručení: </td>
                                <xsl:variable name="date" select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeDelivery/ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime/udt:DateTimeString"/>
                                <xsl:variable name="year" select="substring($date,1,4)"/>
                                <xsl:variable name="month" select="substring($date,5,2)"/>
                                <xsl:variable name="day" select="substring($date,7,2)"/>
                                <td><xsl:value-of select="concat($day, '.', $month, '.', $year)"/></td>
                            </tr>
                            <tr>
                                <td>Datum splatnosti: </td>
                                <xsl:variable name="date" select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:DueDateDateTime/udt:DateTimeString"/>
                                <xsl:variable name="year" select="substring($date,1,4)"/>
                                <xsl:variable name="month" select="substring($date,5,2)"/>
                                <xsl:variable name="day" select="substring($date,7,2)"/>
                                <td><xsl:value-of select="concat($day, '.', $month, '.', $year)"/></td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            <div style="display:table;width:100%">
                <div style="display:table-row">
                    <div style="display:table-cell;width:50%" class="box">
                        <h3 style="text-align: center">Příjemce: </h3>
                        <h4>
                            <xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:Name"/>
                        </h4>
                        <address>
                            <table style="width:100%" class="at">
                                <tr>
                                    <td>Ulice, č.p.: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:LineOne"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>PSČ: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:PostcodeCode"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Město: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:CityName"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Země: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:CountryID"/>
                                    </td>
                                </tr>
                                <xsl:for-each select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:SpecifiedTaxRegistration">
                                    <tr>
                                        <xsl:choose>
                                            <xsl:when test="position() = 1">
                                                <td>DIČ: </td>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <td>IČO: </td>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <td><xsl:value-of select="ram:ID"/></td>
                                    </tr>
                                </xsl:for-each>
                            </table>
                        </address>
                    </div>
                    <div style="display:table-cell" class="box">
                        <h3 style="text-align: center">Odesílatel: </h3>
                        <h4>
                            <xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:Name"/>
                        </h4>
                        <address >
                            <table style="width:100%" class="at">
                                <tr>
                                    <td>Ulice, č.p.: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:LineOne"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>PSČ: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:PostcodeCode"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Město: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:CityName"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Země: </td>
                                    <td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:CountryID"/>
                                    </td>
                                </tr>
                                <xsl:for-each select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedTaxRegistration">
                                    <tr>
                                        <xsl:choose>
                                            <xsl:when test="position() = 1">
                                                <td>DIČ: </td>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <td>IČO: </td>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <td><xsl:value-of select="ram:ID"/></td>
                                    </tr>
                                </xsl:for-each>
                                <tr>
                                    <td>IBAN: </td>
                                    <td>
                                        <xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeePartyCreditorFinancialAccount/ram:IBANID"/>
                                    </td>
                                </tr>
                            </table>
                        </address>
                    </div>

                </div>
            </div>
                <br/>
                <table style="width:100%;border-collapse: collapse;">
                    <tr style="background-color:#F3F5F7;">
                        <th style="text-align:left">Počet</th>
                        <th style="text-align:left">M.J.</th>
                        <th style="text-align:left">Název</th>
                        <th style="text-align:left">Jedn. cena</th>
                        <th style="text-align:left">Sazba DPH(v %)</th>
                        <th style="text-align:left">Základ daně</th>
                    </tr>
                    <xsl:for-each select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:IncludedSupplyChainTradeLineItem">
                        <tr>
                            <td><xsl:value-of select="ram:SpecifiedLineTradeDelivery/ram:BilledQuantity"/></td>
                            <td><xsl:value-of select="ram:SpecifiedLineTradeAgreement/ram:GrossPriceProductTradePrice/ram:BasisQuantity/@unitCode"/></td>
                            <td><xsl:value-of select="ram:SpecifiedTradeProduct/ram:Name"/></td>
                            <td><xsl:value-of select="ram:SpecifiedLineTradeAgreement/ram:GrossPriceProductTradePrice/ram:ChargeAmount"/><xsl:value-of select="$cur"/></td>
                            <td><xsl:value-of select="ram:SpecifiedLineTradeSettlement/ram:ApplicableTradeTax/ram:RateApplicablePercent"/>%</td>
                            <td><xsl:value-of select="ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount"/><xsl:value-of select="$cur"/></td>

                        </tr>
                    </xsl:for-each>
<!--                </table>-->
<!--                <div style="float:right">-->
<!--                    <table border="1">-->
                    <tr style="background-color:#F3F5F7;"><td colspan="5">Bez daně:</td><td><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:LineTotalAmount"/><xsl:value-of select="$cur"/></td></tr>
                        <tr style="font-weight:bold">
                            <td colspan="2"/><td>Kategorie daně</td><td>Procento daně</td><td>Základ daně</td><td>Daň</td></tr>
                        <xsl:for-each select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:ApplicableTradeTax">
                            <tr>
                                <td colspan="2"/>
                                <td><xsl:value-of select="ram:CategoryCode"/></td>
                                <td><xsl:value-of select="ram:RateApplicablePercent"/>%</td>
                                <td><xsl:value-of select="ram:BasisAmount"/><xsl:value-of select="$cur"/></td>
                                <td><xsl:value-of select="ram:CalculatedAmount"/><xsl:value-of select="$cur"/></td>
                            </tr>
                        </xsl:for-each>
<!--                </div>-->
                    <tr><td colspan="5">Celkem:</td><td style="font-weight:bold;font-size:16pt;"><xsl:value-of select="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:GrandTotalAmount"/>
                        <xsl:value-of select="$cur"/>
                    </td></tr>
                    </table>

            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>