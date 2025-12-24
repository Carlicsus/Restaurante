import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// amCharts imports
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import * as am5percent from '@amcharts/amcharts5/percent';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';

@Component({
  selector: 'app-chef-statistics',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './chef-statistics.component.html',
  styleUrls: ['./chef-statistics.component.css']
})
export class ChefStatisticsComponent implements OnInit, AfterViewInit, OnDestroy {
  private root1: am5.Root | undefined;
  private root2: am5.Root | undefined;
  private root3: am5.Root | undefined;

  constructor() { }

  ngOnInit(): void {
    // Inicializar datos si es necesario
  }

  ngAfterViewInit(): void {
    this.createOrdersChart();
    this.createDishesChart();
    this.createTimeChart();
  }

  ngOnDestroy(): void {
    if (this.root1) {
      this.root1.dispose();
    }
    if (this.root2) {
      this.root2.dispose();
    }
    if (this.root3) {
      this.root3.dispose();
    }
  }

  private createOrdersChart(): void {
    // Gráfico de pedidos por día (línea)
    this.root1 = am5.Root.new('ordersChart');

    this.root1.setThemes([am5themes_Animated.new(this.root1)]);

    const chart = this.root1.container.children.push(am5xy.XYChart.new(this.root1, {
      panX: true,
      panY: true,
      wheelX: 'panX',
      wheelY: 'zoomX',
      pinchZoomX: true
    }));

    const cursor = chart.set('cursor', am5xy.XYCursor.new(this.root1, {}));
    cursor.lineY.set('visible', false);

    const xRenderer = am5xy.AxisRendererX.new(this.root1, { minGridDistance: 30 });
    xRenderer.labels.template.setAll({
      rotation: -45,
      centerY: am5.p50,
      centerX: am5.p100,
      paddingRight: 15
    });

    const xAxis = chart.xAxes.push(am5xy.CategoryAxis.new(this.root1, {
      maxDeviation: 0.3,
      categoryField: 'day',
      renderer: xRenderer,
      tooltip: am5.Tooltip.new(this.root1, {})
    }));

    const yAxis = chart.yAxes.push(am5xy.ValueAxis.new(this.root1, {
      maxDeviation: 0.3,
      renderer: am5xy.AxisRendererY.new(this.root1, {})
    }));

    const series = chart.series.push(am5xy.LineSeries.new(this.root1, {
      name: 'Pedidos',
      xAxis: xAxis,
      yAxis: yAxis,
      valueYField: 'orders',
      categoryXField: 'day',
      tooltip: am5.Tooltip.new(this.root1, {
        labelText: '{valueY}'
      })
    }));

    series.strokes.template.setAll({
      strokeWidth: 3,
      stroke: am5.color(0x74b9ff)
    });

    series.bullets.push(() => {
      return am5.Bullet.new(this.root1!, {
        sprite: am5.Circle.new(this.root1!, {
          radius: 5,
          fill: am5.color(0x74b9ff)
        })
      });
    });

    const data = [
      { day: 'Lun', orders: 45 },
      { day: 'Mar', orders: 52 },
      { day: 'Mié', orders: 48 },
      { day: 'Jue', orders: 61 },
      { day: 'Vie', orders: 55 },
      { day: 'Sáb', orders: 67 },
      { day: 'Dom', orders: 43 }
    ];

    xAxis.data.setAll(data);
    series.data.setAll(data);

    series.appear(1000);
    chart.appear(1000, 100);
  }

  private createDishesChart(): void {
    // Gráfico de platos más pedidos (pie)
    this.root2 = am5.Root.new('dishesChart');

    this.root2.setThemes([am5themes_Animated.new(this.root2)]);

    const chart = this.root2.container.children.push(am5percent.PieChart.new(this.root2, {
      layout: this.root2.verticalLayout
    }));

    const series = chart.series.push(am5percent.PieSeries.new(this.root2, {
      valueField: 'orders',
      categoryField: 'dish',
      alignLabels: false
    }));

    series.labels.template.setAll({
      text: '{category}: {value}',
      fontSize: 12
    });

    series.slices.template.setAll({
      stroke: am5.color(0xffffff),
      strokeWidth: 2
    });

    const data = [
      { dish: 'Ensalada César', orders: 120 },
      { dish: 'Pollo al Limón', orders: 95 },
      { dish: 'Tacos de Pescado', orders: 78 },
      { dish: 'Hamburguesa', orders: 65 },
      { dish: 'Sopa de Tomate', orders: 52 }
    ];

    series.data.setAll(data);

    series.appear(1000, 100);
  }

  private createTimeChart(): void {
    // Gráfico de pedidos por hora (barra)
    this.root3 = am5.Root.new('timeChart');

    this.root3.setThemes([am5themes_Animated.new(this.root3)]);

    const chart = this.root3.container.children.push(am5xy.XYChart.new(this.root3, {
      panX: false,
      panY: false,
      wheelX: 'none',
      wheelY: 'none'
    }));

    const yRenderer = am5xy.AxisRendererY.new(this.root3, {});
    const yAxis = chart.yAxes.push(am5xy.CategoryAxis.new(this.root3, {
      categoryField: 'hour',
      renderer: yRenderer
    }));

    const xAxis = chart.xAxes.push(am5xy.ValueAxis.new(this.root3, {
      renderer: am5xy.AxisRendererX.new(this.root3, {})
    }));

    const series = chart.series.push(am5xy.ColumnSeries.new(this.root3, {
      name: 'Pedidos',
      xAxis: xAxis,
      yAxis: yAxis,
      valueXField: 'orders',
      categoryYField: 'hour',
      tooltip: am5.Tooltip.new(this.root3, {
        labelText: '{valueX}'
      })
    }));

    series.columns.template.setAll({
      fill: am5.color(0x00b894),
      stroke: am5.color(0x00a085)
    });

    const data = [
      { hour: '12:00', orders: 15 },
      { hour: '13:00', orders: 25 },
      { hour: '14:00', orders: 30 },
      { hour: '15:00', orders: 20 },
      { hour: '16:00', orders: 18 },
      { hour: '17:00', orders: 22 },
      { hour: '18:00', orders: 35 },
      { hour: '19:00', orders: 40 },
      { hour: '20:00', orders: 28 }
    ];

    yAxis.data.setAll(data);
    series.data.setAll(data);

    series.appear(1000);
    chart.appear(1000, 100);
  }
}
