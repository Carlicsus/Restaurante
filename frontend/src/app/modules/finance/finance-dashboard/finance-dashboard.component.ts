import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarFinanceComponent } from '../../../shared/navbar-finance/navbar-finance.component';
import { SaleService } from '../../../core/services/sale.service';
import { Router } from '@angular/router';
import { Debtor } from '../../../core/models/payment';

import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import * as am5percent from '@amcharts/amcharts5/percent';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';

interface Stat {
  title: string;
  value: string;
  change: string;
  positive: boolean;
  loading?: boolean;
}

@Component({
  selector: 'app-finance-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    NavbarFinanceComponent
  ],
  templateUrl: './finance-dashboard.component.html',
  styleUrls: ['./finance-dashboard.component.css']
})
export class FinanceDashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  private rootBar: am5.Root | undefined;
  private rootPie: am5.Root | undefined;
  private debtorsData: Debtor[] = [];

  stats: Stat[] = [
    {
      title: 'Órdenes Pendientes',
      value: '0',
      change: 'Cargando...',
      positive: true,
      loading: true
    },
    {
      title: 'Monto Total Adeudado',
      value: '$0.00',
      change: 'Cargando...',
      positive: false,
      loading: true
    },
    {
      title: 'Empleados Deudores',
      value: '0',
      change: 'Cargando...',
      positive: true,
      loading: true
    }
  ];

  errorMessage = '';
  hasError = false;

  constructor(
    private saleService: SaleService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadFinanceData();
  }

  ngAfterViewInit(): void {
    // Las gráficas se crearán después de cargar los datos
  }

  ngOnDestroy(): void {
    if (this.rootBar) {
      this.rootBar.dispose();
    }
    if (this.rootPie) {
      this.rootPie.dispose();
    }
  }

  loadFinanceData(): void {
    console.log('Cargando datos financieros...');
    console.log('URL:', `http://localhost:8080/api/sale/debtors/all`);
    
    this.saleService.listDebtors().subscribe({
      next: (response) => {
        console.log('Respuesta recibida:', response);
        
        if (response.success && response.data) {
          const { debtors, summary } = response.data;
          
          console.log('Deudores:', debtors);
          console.log('Resumen:', summary);

          // Guardar datos para las gráficas
          this.debtorsData = debtors;
          
          // Calcular total de órdenes pendientes
          const totalOrders = debtors.reduce((sum, d) => sum + d.pendingOrdersCount, 0);
          
          // Actualizar estadísticas
          this.stats[0] = {
            title: 'Órdenes Pendientes',
            value: totalOrders.toString(),
            change: `${debtors.length} empleados`,
            positive: true,
            loading: false
          };

          this.stats[1] = {
            title: 'Monto Total Adeudado',
            value: this.formatCurrency(summary.totalDebtAmount),
            change: totalOrders > 0 ? `Promedio: ${this.formatCurrency(summary.totalDebtAmount / totalOrders)}` : 'Sin deuda',
            positive: false,
            loading: false
          };

          this.stats[2] = {
            title: 'Empleados Deudores',
            value: summary.totalDebtors.toString(),
            change: summary.totalDebtors > 0 ? 'Requiere atención' : 'Todo pagado',
            positive: summary.totalDebtors === 0,
            loading: false
          };

          this.hasError = false;

          // Crear gráficas después de cargar los datos
          setTimeout(() => {
            this.createDebtorsBarChart();
            this.createDebtorsPieChart();
          }, 100);
        }
      },
      error: (error) => {
        console.error('Error al cargar datos financieros:', error);
        console.error('Detalles del error:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url
        });
        
        this.hasError = true;
        
        if (error.status === 0) {
          this.errorMessage = 'No se puede conectar con el servidor. Verifica que el backend esté corriendo en http://localhost:8080';
        } else if (error.status === 404) {
          this.errorMessage = 'Endpoint no encontrado. Verifica la URL del API.';
        } else if (error.status === 500) {
          this.errorMessage = 'Error en el servidor. Revisa los logs del backend.';
        } else {
          this.errorMessage = `Error al cargar datos: ${error.statusText || 'Error desconocido'}`;
        }
        
        // Mostrar valores por defecto en caso de error
        this.stats.forEach(stat => {
          stat.loading = false;
          stat.change = 'Error al cargar';
        });
      }
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0
    }).format(value / 100); // El backend guarda en centavos
  }

  goToDebtManagement(): void {
    this.router.navigate(['/finance/debt']);
  }

  goToPaymentManagement(): void {
    this.router.navigate(['/finance/payment-employee']);
  }

  retryLoad(): void {
    this.hasError = false;
    this.stats.forEach(stat => stat.loading = true);
    this.loadFinanceData();
  }

  private createDebtorsBarChart(): void {
    if (this.rootBar) {
      this.rootBar.dispose();
    }

    this.rootBar = am5.Root.new('debtorsBarChart');
    this.rootBar.setThemes([am5themes_Animated.new(this.rootBar)]);

    const chart = this.rootBar.container.children.push(
      am5xy.XYChart.new(this.rootBar, {
        panX: false,
        panY: false,
        wheelX: 'none',
        wheelY: 'none',
        layout: this.rootBar.verticalLayout
      })
    );

    // Ordenar por deuda descendente y tomar top 10
    const top10Debtors = [...this.debtorsData]
      .sort((a, b) => b.totalPendingAmount - a.totalPendingAmount)
      .slice(0, 10)
      .map(d => ({
        username: d.username,
        debt: d.totalPendingAmount / 100, // Convertir centavos a pesos
        orders: d.pendingOrdersCount
      }));

    // Eje X (categorías)
    const xRenderer = am5xy.AxisRendererX.new(this.rootBar, {
      minGridDistance: 30
    });
    xRenderer.labels.template.setAll({
      rotation: -45,
      centerY: am5.p50,
      centerX: am5.p100,
      paddingRight: 15,
      fontSize: 12
    });

    const xAxis = chart.xAxes.push(
      am5xy.CategoryAxis.new(this.rootBar, {
        categoryField: 'username',
        renderer: xRenderer,
        tooltip: am5.Tooltip.new(this.rootBar, {})
      })
    );

    xAxis.data.setAll(top10Debtors);

    // Eje Y (valores)
    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(this.rootBar, {
        renderer: am5xy.AxisRendererY.new(this.rootBar, {})
      })
    );

    // Serie de barras
    const series = chart.series.push(
      am5xy.ColumnSeries.new(this.rootBar, {
        name: 'Deuda',
        xAxis: xAxis,
        yAxis: yAxis,
        valueYField: 'debt',
        categoryXField: 'username',
        tooltip: am5.Tooltip.new(this.rootBar, {
          labelText: '{username}\nDeuda: ${valueY}\nÓrdenes: {orders}'
        })
      })
    );

    series.columns.template.setAll({
      cornerRadiusTL: 8,
      cornerRadiusTR: 8,
      strokeOpacity: 0,
      fillGradient: am5.LinearGradient.new(this.rootBar, {
        stops: [
          { color: am5.color(0xef4444) },
          { color: am5.color(0xdc2626) }
        ]
      })
    });

    series.data.setAll(top10Debtors);

    // Título
    chart.children.unshift(
      am5.Label.new(this.rootBar, {
        text: 'Empleados con Mayor Deuda',
        fontSize: 16,
        fontWeight: '600',
        textAlign: 'center',
        x: am5.percent(50),
        centerX: am5.percent(50),
        paddingTop: 0,
        paddingBottom: 10
      })
    );

    series.appear(1000);
    chart.appear(1000, 100);
  }

  private createDebtorsPieChart(): void {
    if (this.rootPie) {
      this.rootPie.dispose();
    }

    this.rootPie = am5.Root.new('debtorsPieChart');
    this.rootPie.setThemes([am5themes_Animated.new(this.rootPie)]);

    const chart = this.rootPie.container.children.push(
      am5percent.PieChart.new(this.rootPie, {
        layout: this.rootPie.verticalLayout,
        innerRadius: am5.percent(50)
      })
    );

    // Preparar datos: tomar top 8 y agrupar el resto
    const sortedDebtors = [...this.debtorsData]
      .sort((a, b) => b.totalPendingAmount - a.totalPendingAmount);

    const top8 = sortedDebtors.slice(0, 8).map(d => ({
      username: d.username,
      debt: d.totalPendingAmount / 100
    }));

    const others = sortedDebtors.slice(8).reduce((sum, d) => sum + d.totalPendingAmount, 0) / 100;
    
    if (others > 0) {
      top8.push({
        username: 'Otros',
        debt: others
      });
    }

    // Serie
    const series = chart.series.push(
      am5percent.PieSeries.new(this.rootPie, {
        valueField: 'debt',
        categoryField: 'username',
        alignLabels: false
      })
    );

    series.labels.template.setAll({
      text: '{category}',
      fontSize: 11,
      fill: am5.color(0x000000)
    });

    series.slices.template.setAll({
      strokeWidth: 2,
      stroke: am5.color(0xffffff),
      tooltipText: '{category}: ${value} ({valuePercentTotal.formatNumber(\'0.00\')}%)'
    });

    // Colores personalizados
    series.get('colors')?.set('colors', [
      am5.color(0xef4444),
      am5.color(0xf97316),
      am5.color(0xf59e0b),
      am5.color(0xeab308),
      am5.color(0x84cc16),
      am5.color(0x22c55e),
      am5.color(0x10b981),
      am5.color(0x14b8a6),
      am5.color(0x6b7280)
    ]);

    series.data.setAll(top8);

    // Título
    chart.children.unshift(
      am5.Label.new(this.rootPie, {
        text: 'Distribución de Deuda',
        fontSize: 16,
        fontWeight: '600',
        textAlign: 'center',
        x: am5.percent(50),
        centerX: am5.percent(50),
        paddingTop: 0,
        paddingBottom: 10
      })
    );

    series.appear(1000, 100);
  }
}
