import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './landing.component.html',
    styleUrls: ['./landing.component.css']
})
export class LandingComponent {
    experienceItems = [
        {
            title: 'PROCESOS ESTABLECIDOS',
            description: 'Sistemas de operación estandarizados que garantizan calidad y eficiencia en cada servicio desde nuestra fundación.'
        },
        {
            title: 'EQUIPO ESPECIALIZADO',
            description: 'Personal capacitado en diferentes áreas gastronómicas, desde cocina internacional hasta servicio especializado.'
        },
        {
            title: 'CALIDAD CONSISTENTE',
            description: 'Control de proveedores y procesos que aseguran uniformidad en cada uno de nuestros servicios corporativos.'
        },
        {
            title: 'CRECIMIENTO COMPROBADO',
            description: 'Más de 15 años expandiendo nuestros servicios a diferentes sectores corporativos e institucionales.'
        },
        {
            title: 'COMPROMISO OPERATIVO',
            description: 'Protocolos de seguridad alimentaria y sostenibilidad aplicados en todas nuestras operaciones.'
        },
        {
            title: 'SOLUCIONES ADAPTABLES',
            description: 'Menús y servicios personalizables según las necesidades específicas de cada cliente corporativo.'
        }
    ];

    menuCategories = [
        {
            name: 'ENTRADAS',
            items: ['Guacamole con Totopos', 'Ensalada César', 'Sopa de Tortilla'],
            price: '$120 - $180'
        },
        {
            name: 'PLATOS FUERTES',
            items: ['Tacos al Pastor', 'Enchiladas Suizas', 'Mole Poblano'],
            price: '$200 - $350'
        },
        {
            name: 'POSTRES',
            items: ['Flan Napolitano', 'Churros', 'Pastel de Tres Leches'],
            price: '$90 - $150'
        }
    ];

    testimonials = [
        {
            name: 'Jake Judge',
            role: 'Food Critic',
            comment: '"Latin cooking I was never really interested in anything until I tried here."',
            avatar: 'JJ'
        },
        {
            name: 'Kevin Ourstory',
            role: 'Regular Customer',
            comment: '"The flavors are authentic and the service is exceptional. A true culinary experience."',
            avatar: 'KO'
        }
    ];

    quickStats = [
        { number: '15+', label: 'Years Experience' },
        { number: '200+', label: 'Menu Items' },
        { number: '98%', label: 'Customer Satisfaction' },
        { number: '50+', label: 'Expert Chefs' }
    ];
}