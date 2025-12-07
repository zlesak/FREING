import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';

Chart.register(...registerables, ChartDataLabels);

@Component({
  selector: 'invoice-chart-doughnut',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice-chart-doughnut.html',
  styleUrls: ['./invoice-chart-doughnut.css']
})
export class InvoiceChartDoughnut implements AfterViewInit, OnChanges {
  data = input.required<{ itemName: string, amount: number, color: string }[]>();
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

    const itemNames = this.data().map(d => d.itemName);
    const amounts = this.data().map(d => d.amount);
    const backgroundColors = this.data().map(d => d.color);
    const borderColors = this.data().map(d => this.darkenColor(d.color));

    const chartData = {
      labels: itemNames,
      datasets: [{
        data: amounts,
        backgroundColor: backgroundColors,
        borderColor: borderColors,
        borderWidth: 2,
      }],
    };

    const config: ChartConfiguration = {
      type: 'doughnut' as ChartType,
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
              pointStyle: 'circle',
              color: '#333'
            }
          },
          title: {
            display: true,
            text: this.title(),
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
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = (context.dataset.data as number[]).reduce((a, b) => a + b, 0);
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
                return `${label}: ${value.toFixed(2)} Kč (${percentage}%)`;
              }
            }
          },
          datalabels: {
            color: '#fff',
            font: {
              weight: 'bold',
              size: 13
            },
            formatter: (value: any, context: any) => {
              const total = (context.dataset.data as number[]).reduce((a: number, b: number) => a + b, 0);
              const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
              return `${value.toFixed(0)} Kč\n(${percentage}%)`;
            },
            textAlign: 'center',
            anchor: 'center',
            align: 'center',
            offset: 0,
            backgroundColor: (context: any) => {
              return 'rgba(0, 0, 0, 0.7)';
            },
            borderRadius: 4,
            padding: 6
          }
        }
      },
    };

    this.chart = new Chart(ctx, config);
  }

  private updateChart(): void {
    const itemNames = this.data().map(d => d.itemName);
    const amounts = this.data().map(d => d.amount);
    const backgroundColors = this.data().map(d => d.color);
    const borderColors = this.data().map(d => this.darkenColor(d.color));

    this.chart.data.labels = itemNames;
    this.chart.data.datasets[0].data = amounts;
    this.chart.data.datasets[0].backgroundColor = backgroundColors;
    (this.chart.data.datasets[0] as any).borderColor = borderColors;

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
