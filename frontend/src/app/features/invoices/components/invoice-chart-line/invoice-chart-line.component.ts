import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { Invoice } from '../../../../api/generated/invoice';
import {InvoiceStatus} from '../../../common/Enums.js';

Chart.register(...registerables);

@Component({
  selector: 'invoice-chart-line',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice-chart-line.component.html',
  styleUrls: ['./invoice-chart-line.component.css']
})
export class InvoiceChartLine implements AfterViewInit, OnChanges {
  data = input.required<Invoice[]>();
  @ViewChild("chartCanvas") chartCanvas!: ElementRef<HTMLCanvasElement>;

  private chart!: Chart;

  ngAfterViewInit(): void {
    this.renderChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] && this.chart) {
      this.updateChart();
    }
  }

  private processData(invoices: Invoice[]): { labels: string[], issuedData: number[], paidData: number[] } {
    const sortedInvoices = [...invoices].sort((a, b) => new Date(a.issueDate).getTime() - new Date(b.issueDate).getTime());

    const aggregatedData: Record<string, { issued: number, paid: number }> = {};

    sortedInvoices.forEach(inv => {
      const dateKey = new Date(inv.issueDate).toISOString().substring(0, 7); // YYYY-MM

      if (!aggregatedData[dateKey]) {
        aggregatedData[dateKey] = { issued: 0, paid: 0 };
      }

      aggregatedData[dateKey].issued += inv.amount;

      if (inv.status === InvoiceStatus.PAID || inv.status === InvoiceStatus.PAID_OVERDUE) {
        aggregatedData[dateKey].paid += inv.amount;
      }
    });

    const labels = Object.keys(aggregatedData).sort();
    const issuedData = labels.map(key => aggregatedData[key].issued);
    const paidData = labels.map(key => aggregatedData[key].paid);

    return { labels, issuedData, paidData };
  }

  private renderChart(): void {
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const { labels, issuedData, paidData } = this.processData(this.data());

    const chartData = {
      labels: labels,
      datasets: [
        {
          label: 'Celkem Vystaveno',
          data: issuedData,
          borderColor: '#9a9a9c',
          backgroundColor: 'rgba(109,109,110,0.2)',
          tension: 0.4,
          fill: false,
        },
        {
          label: 'Celkem Zaplaceno',
          data: paidData,
          borderColor: '#6265ef',
          backgroundColor: 'rgb(63,80,180)',
          tension: 0.4,
          fill: false,
        }
      ],
    };

    const config: ChartConfiguration = {
      type: 'line' as ChartType,
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' },
          title: {
            display: true,
            text: 'Částka Vystavených a Zaplacených Faktur v Čase',
            position: 'bottom'
          }
        },
        scales: {
          x: { title: { display: true, text: 'Měsíc / Datum' } },
          y: { title: { display: true, text: 'Částka' } }
        }
      },
    };

    this.chart = new Chart(ctx, config);
  }

  private updateChart(): void {
    const { labels, issuedData, paidData } = this.processData(this.data());

    if (this.chart.data.datasets.length < 2) {
      this.chart.destroy();
      this.renderChart();
      return;
    }

    this.chart.data.labels = labels;
    this.chart.data.datasets[0].data = issuedData;
    this.chart.data.datasets[1].data = paidData;

    this.chart.update();
  }
}
