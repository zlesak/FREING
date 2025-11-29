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

    const chartData = {
      labels: labels,
      datasets: [{
        label: 'Počet Faktur',
        data: counts,
        backgroundColor: '#90a4ae',
        borderColor: '#546e7a',
        borderWidth: 1,
      }],
    };

    const config: ChartConfiguration = {
      type: 'bar' as ChartType,
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          title: {
            display: true,
            text: 'Rozdělení Počtu Faktur Dle Částky (Kč/Měna)',
            position: 'bottom'
          }
        },
        scales: {
          x: {
            title: { display: true, text: 'Interval Částky' }
          },
          y: {
            beginAtZero: true,
            title: { display: true, text: 'Počet Faktur' }
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
