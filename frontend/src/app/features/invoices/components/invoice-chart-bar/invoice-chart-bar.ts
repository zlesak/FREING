import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { Invoice } from '../../../../api/generated/invoice';

Chart.register(...registerables);

@Component({
  selector: 'invoice-chart-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice-chart-bar.html',
  styleUrls: ['./invoice-chart-bar.css']
})
export class InvoiceChartBar implements AfterViewInit, OnChanges {
  data = input.required<Invoice[]>();
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

  private processData(invoices: Invoice[]): { labels: string[], counts: number[] } {
    const bins = [0, 500, 1000, 5000, 10000];
    const labels = bins.map((min, index) => {
      const max = bins[index + 1];
      if (max === undefined) return `>${min}`;
      return `${min} - ${max}`;
    });

    const counts = new Array(bins.length).fill(0);

    invoices.forEach(inv => {
      let binIndex = -1;
      for (let i = 0; i < bins.length; i++) {
        const min = bins[i];
        const max = bins[i + 1];

        if (max === undefined) {
          if (inv.amount >= min) {
            binIndex = i;
          }
        } else if (inv.amount >= min && inv.amount < max) {
          binIndex = i;
        }

        if (binIndex !== -1) {
          counts[binIndex]++;
          break;
        }
      }
    });
    return { labels, counts };
  }

  private renderChart(): void {
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const { labels, counts } = this.processData(this.data());

    // Vytvoření gradientu pro bars
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, '#42a5f5');
    gradient.addColorStop(0.5, '#1e88e5');
    gradient.addColorStop(1, '#1565c0');

    const chartData = {
      labels: labels,
      datasets: [{
        label: 'Počet Faktur',
        data: counts,
        backgroundColor: gradient,
        borderColor: '#0d47a1',
        borderWidth: 2,
        borderRadius: 8,
        borderSkipped: false,
      }],
    };

    const config: ChartConfiguration = {
      type: 'bar' as ChartType,
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
            text: 'Rozdělení Počtu Faktur Dle Částky',
            position: 'top',
            font: { size: 18, weight: 'bold' },
            padding: { top: 5, bottom: 15 },
            color: '#1a237e'
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.9)',
            padding: 16,
            cornerRadius: 10,
            titleFont: { size: 14, weight: 'bold' },
            bodyFont: { size: 13 },
            callbacks: {
              label: (context) => {
                const value = context.parsed.y || 0;
                return `Počet Faktur: ${value}`;
              }
            }
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Interval Částky',
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
    const { labels, counts } = this.processData(this.data());

    this.chart.data.labels = labels;
    this.chart.data.datasets[0].data = counts;

    this.chart.update();
  }
}
