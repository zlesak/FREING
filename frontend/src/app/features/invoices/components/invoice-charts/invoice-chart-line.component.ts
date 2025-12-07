import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { Invoice } from '../../../../api/generated/invoice';
import {InvoiceStatus} from '../../../common/Enums.js.js';

Chart.register(...registerables);

@Component({
  selector: 'invoice-chart-line',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './invoice-chart-base.html',
  styleUrls: ['./chart-common.css']
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

    // Gradientní výplň pro vystavené faktury
    const gradientIssued = ctx.createLinearGradient(0, 0, 0, 400);
    gradientIssued.addColorStop(0, 'rgba(158, 158, 158, 0.3)');
    gradientIssued.addColorStop(1, 'rgba(158, 158, 158, 0.05)');

    // Gradientní výplň pro zaplacené faktury
    const gradientPaid = ctx.createLinearGradient(0, 0, 0, 400);
    gradientPaid.addColorStop(0, 'rgba(76, 175, 80, 0.4)');
    gradientPaid.addColorStop(1, 'rgba(76, 175, 80, 0.05)');

    const chartData = {
      labels: labels,
      datasets: [
        {
          label: 'Celkem Vystaveno',
          data: issuedData,
          borderColor: '#757575',
          backgroundColor: gradientIssued,
          borderWidth: 3,
          tension: 0.4,
          fill: true,
          pointRadius: 5,
          pointHoverRadius: 7,
          pointBackgroundColor: '#757575',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverBackgroundColor: '#424242',
          pointHoverBorderColor: '#fff',
        },
        {
          label: 'Celkem Zaplaceno',
          data: paidData,
          borderColor: '#4caf50',
          backgroundColor: gradientPaid,
          borderWidth: 3,
          tension: 0.4,
          fill: true,
          pointRadius: 5,
          pointHoverRadius: 7,
          pointBackgroundColor: '#4caf50',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverBackgroundColor: '#2e7d32',
          pointHoverBorderColor: '#fff',
        }
      ],
    };

    const config: ChartConfiguration = {
      type: 'line' as ChartType,
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        layout: {
          padding: {
            top: 20,
            bottom: 10
          }
        },
        interaction: {
          mode: 'index',
          intersect: false,
        },
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            labels: {
              padding: 15,
              font: { size: 12, weight: 'bold' },
              usePointStyle: true,
              pointStyle: 'circle',
              color: '#333'
            }
          },
          title: {
            display: true,
            text: 'Částka Vystavených a Zaplacených Faktur v Čase',
            position: 'top',
            font: { size: 18, weight: 'bold' },
            padding: { top: 5, bottom: 15 },
            color: '#1a237e'
          },
          tooltip: {
            mode: 'index',
            intersect: false,
            backgroundColor: 'rgba(0, 0, 0, 0.9)',
            padding: 16,
            cornerRadius: 10,
            titleFont: { size: 14, weight: 'bold' },
            bodyFont: { size: 13 },
            bodySpacing: 8
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Měsíc / Datum',
              font: { size: 13, weight: 'bold' },
              color: '#555'
            },
            grid: { display: false },
            ticks: {
              font: { size: 11 },
              color: '#666'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Částka',
              font: { size: 13, weight: 'bold' },
              color: '#555'
            },
            beginAtZero: true,
            grid: {
              color: 'rgba(0, 0, 0, 0.06)'
            },
            ticks: {
              font: { size: 11 },
              color: '#666'
            }
          }
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
