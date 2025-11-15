import { Component, AfterViewInit, OnChanges, SimpleChanges, ElementRef, ViewChild, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import {getStatusColor} from '../../../home/view/home-page.component';

Chart.register(...registerables);

@Component({
  selector: 'invoice-chart-pie',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice-chart-pie.html',
  styleUrls: ['./invoice-chart-pie.css']
})
export class InvoiceChartPie implements AfterViewInit, OnChanges {
  data = input.required<{ status: string; occurrence: number }[]>();
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

    // compute once
    const statuses = this.data().map(d => d.status);
    const occurrences = this.data().map(d => d.occurrence);
    const backgroundColors = this.data().map(d => getStatusColor(d.status).background);
    const borderColors = this.data().map(d => getStatusColor(d.status).color);

    const chartData = {
      labels: statuses,
      datasets: [{
        data: occurrences,
        backgroundColor: backgroundColors,
        borderColor: borderColors,
        borderWidth: 1,
      }],
    };

    const config: ChartConfiguration = {
      type: 'pie' as ChartType,
      data: chartData,
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' },
          title: { display: true, text: this.title() }
        }
      },
    };

    this.chart = new Chart(ctx, config);
  }

  private updateChart(): void {
    const statuses = this.data().map(d => d.status);
    const occurrences = this.data().map(d => d.occurrence);
    const backgroundColors = this.data().map(d => getStatusColor(d.status).background);
    const borderColors = this.data().map(d => getStatusColor(d.status).color);

    this.chart.data.labels = statuses;
    this.chart.data.datasets[0].data = occurrences;
    this.chart.data.datasets[0].backgroundColor = backgroundColors;
    (this.chart.data.datasets[0] as any).borderColor = borderColors;

    this.chart.update();
  }
}

