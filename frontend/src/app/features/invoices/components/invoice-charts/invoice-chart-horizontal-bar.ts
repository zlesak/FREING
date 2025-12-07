import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'invoice-chart-horizontal-bar',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './invoice-chart-base.html',
  styleUrls: ['./chart-common.css']
})
export class InvoiceChartHorizontalBar implements AfterViewInit, OnChanges {
  data = input.required<{ customerName: string, totalAmount: number, color: string }[]>();
  title = input<string>();
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

  private renderChart(): void {
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    // Take top 10 customers
    const sortedData = [...this.data()].sort((a, b) => b.totalAmount - a.totalAmount).slice(0, 10);

    const customerNames = sortedData.map(d => d.customerName);
    const amounts = sortedData.map(d => d.totalAmount);

    // Vytvoření gradient barev pro každý bar
    const backgroundColors = sortedData.map((d, index) => {
      const gradient = ctx.createLinearGradient(0, 0, 400, 0);
      const baseColor = d.color;
      gradient.addColorStop(0, this.lightenColor(baseColor, 0.3));
      gradient.addColorStop(1, baseColor);
      return gradient;
    });

    const borderColors = sortedData.map(d => this.darkenColor(d.color));

    const chartData = {
      labels: customerNames,
      datasets: [{
        label: 'Celková Částka (Kč)',
        data: amounts,
        backgroundColor: backgroundColors,
        borderColor: borderColors,
        borderWidth: 2,
        borderRadius: 6,
        borderSkipped: false,
      }],
    };

    const config: ChartConfiguration = {
      type: 'bar' as ChartType,
      data: chartData,
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        layout: {
          padding: {
            top: 20,
            bottom: 10,
            left: 10,
            right: 20
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
            text: this.title() || 'Top 10 Zákazníků Dle Celkové Částky',
            position: "top",
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
                const value = context.parsed.x || 0;
                return `Celková částka: ${value.toFixed(2)} Kč`;
              }
            }
          }
        },
        scales: {
          x: {
            beginAtZero: true,
            title: {
              display: true,
              text: 'Celková Částka (Kč)',
              font: { size: 13, weight: 'bold' },
              color: '#555'
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.06)'
            },
            ticks: {
              font: { size: 11 },
              color: '#666'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Zákazník',
              font: { size: 13, weight: 'bold' },
              color: '#555'
            },
            grid: { display: false },
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
    const sortedData = [...this.data()].sort((a, b) => b.totalAmount - a.totalAmount).slice(0, 10);

    const customerNames = sortedData.map(d => d.customerName);
    const amounts = sortedData.map(d => d.totalAmount);
    const backgroundColors = sortedData.map(d => d.color);
    const borderColors = sortedData.map(d => this.darkenColor(d.color));

    this.chart.data.labels = customerNames;
    this.chart.data.datasets[0].data = amounts;
    this.chart.data.datasets[0].backgroundColor = backgroundColors;
    (this.chart.data.datasets[0] as any).borderColor = borderColors;

    this.chart.update();
  }

  private lightenColor(color: string, amount: number = 0.3): string {
    if (color.startsWith('#')) color = color.slice(1);

    const num = parseInt(color, 16);
    let r = (num >> 16) & 0xFF;
    let g = (num >> 8) & 0xFF;
    let b = num & 0xFF;

    r = Math.min(255, Math.floor(r + (255 - r) * amount));
    g = Math.min(255, Math.floor(g + (255 - g) * amount));
    b = Math.min(255, Math.floor(b + (255 - b) * amount));

    return `#${r.toString(16).padStart(2,'0')}${g.toString(16).padStart(2,'0')}${b.toString(16).padStart(2,'0')}`;
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
