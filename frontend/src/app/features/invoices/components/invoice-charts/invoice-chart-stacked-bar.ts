import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { Invoice } from '../../../../api/generated/invoice';
import { InvoiceStatusTranslationService } from '../../../common/controller/invoice-status-translation.service';

Chart.register(...registerables);

interface MonthlyData {
  [month: string]: {
    [status: string]: number;
  };
}

@Component({
  selector: 'invoice-chart-stacked-bar',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './invoice-chart-base.html',
  styleUrls: ['./chart-common.css']
})
export class InvoiceChartStackedBar implements AfterViewInit, OnChanges {
  private readonly statusTranslation = inject(InvoiceStatusTranslationService);
  data = input.required<Invoice[]>();
  statusColors = input.required<{ [status: string]: string }>();
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  private chart!: Chart;

  ngAfterViewInit(): void {
    this.renderChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data'] && this.chart) {
      this.updateChart();
    }
  }

  private processData(invoices: Invoice[]): { labels: string[], datasets: any[] } {
    const monthlyData: MonthlyData = {};
    const statusSet = new Set<string>();

    invoices.forEach(inv => {
      const dateKey = new Date(inv.issueDate).toISOString().substring(0, 7);
      const status = inv.status || 'Unknown';

      if (!monthlyData[dateKey]) {
        monthlyData[dateKey] = {};
      }
      if (!monthlyData[dateKey][status]) {
        monthlyData[dateKey][status] = 0;
      }

      monthlyData[dateKey][status] += 1;
      statusSet.add(status);
    });

    const labels = Object.keys(monthlyData).sort();
    const statuses = Array.from(statusSet);
    const colors = this.statusColors();

    const datasets = statuses.map(status => ({
      label: this.statusTranslation.getStatusLabel(status as any),
      data: labels.map(month => monthlyData[month][status] || 0),
      backgroundColor: colors[status] || '#b0bec5',
      borderColor: this.darkenColor(colors[status] || '#b0bec5'),
      borderWidth: 2,
      borderRadius: 6,
      borderSkipped: false,
    }));

    return { labels, datasets };
  }

  private renderChart(): void {
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const { labels, datasets } = this.processData(this.data());

    const config: ChartConfiguration = {
      type: 'bar' as ChartType,
      data: { labels, datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        layout: {
          padding: {
            top: 20,
            bottom: 10
          }
        },
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            labels: {
              padding: 15,
              font: { size: 12, weight: 'bold' },
              usePointStyle: true,
              pointStyle: 'rect',
              color: '#333'
            }
          },
          title: {
            display: true,
            text: 'Měsíční Rozdělení Faktur Dle Statusu',
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
            bodySpacing: 6
          }
        },
        scales: {
          x: {
            stacked: true,
            title: {
              display: true,
              text: 'Měsíc',
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
            stacked: true,
            beginAtZero: true,
            title: {
              display: true,
              text: 'Počet Faktur',
              font: { size: 13, weight: 'bold' },
              color: '#555'
            },
            ticks: {
              stepSize: 1,
              font: { size: 11 },
              color: '#666'
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.06)'
            }
          }
        }
      },
    };

    this.chart = new Chart(ctx, config);
  }

  private updateChart(): void {
    const { labels, datasets } = this.processData(this.data());

    this.chart.data.labels = labels;
    this.chart.data.datasets = datasets;

    this.chart.update();
  }

  private darkenColor(color: string, amount: number = 0.4): string {
    if (color.startsWith('#')) color = color.slice(1);

    const num = parseInt(color, 16);
    let r = (num >> 16) & 0xFF;
    let g = (num >> 8) & 0xFF;
    let b = num & 0xFF;

    r = Math.max(0, Math.floor(r * (1 - amount)));
    g = Math.max(0, Math.floor(g * (1 - amount)));
    b = Math.max(0, Math.floor(b * (1 - amount)));

    return `#${r.toString(16).padStart(2,'0')}${g.toString(16).padStart(2,'0')}${b.toString(16).padStart(2,'0')}`;
  }
}
