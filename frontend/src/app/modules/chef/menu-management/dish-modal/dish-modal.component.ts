import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Dish, Menu } from '../../../../core/models/dish';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
    selector: 'app-dish-modal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './dish-modal.component.html',
    styleUrls: ['./dish-modal.component.css']
})
export class DishModalComponent {
    @Input() isOpen = false;
    @Input() mode: 'create' | 'clone' = 'create';
    @Input() menuTypes: Menu[] = [];
    @Output() close = new EventEmitter<void>();
    @Output() save = new EventEmitter<any>();

    constructor(private notificationService: NotificationService) {}

    form = {
        name: '',
        description: '',
        cost: 0,
        menuType: ''
    };

    selectedImage: File | null = null;
    imagePreview: string | null = null;
    selectedDish: Dish | null = null;

    openModal(dishToClone?: Dish) {
        console.log('Modal abierto, menuTypes:', this.menuTypes);
        console.log('Modo:', this.mode);
        if (dishToClone && this.mode === 'clone') {
            this.selectedDish = dishToClone;
            this.form = {
                name: dishToClone.name + ' (Clonado)',
                description: dishToClone.description,
                cost: dishToClone.cost / 100,
                menuType: typeof dishToClone.menuType === 'string'
                    ? dishToClone.menuType
                    : (dishToClone.menuType as any)?.uuid || ''
            };
        } else {
            this.selectedDish = null;
            this.resetForm();
        }
    }

    resetForm() {
        this.form = {
            name: '',
            description: '',
            cost: 0,
            menuType: ''
        };
        this.selectedImage = null;
        this.imagePreview = null;
    }

    onImageSelected(event: any) {
        const file = event.target.files[0];
        if (file) {
            if (!file.type.startsWith('image/')) {
                this.notificationService.error('Por favor selecciona un archivo de imagen válido');
                return;
            }
            
            if (file.size > 5 * 1024 * 1024) {
                this.notificationService.error('La imagen no debe superar los 5MB');
                return;
            }
            
            this.selectedImage = file;
            
            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.imagePreview = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }

    removeImage() {
        this.selectedImage = null;
        this.imagePreview = null;
    }

    onSubmit() {
        if (this.form.name.trim() && this.form.cost > 0 && this.form.menuType) {
            const dataToEmit: any = {
                name: this.form.name,
                description: this.form.description,
                cost: this.form.cost,
                menuType: this.form.menuType,
                image: this.selectedImage
            };
            
            if (this.mode === 'clone' && this.selectedDish) {
                dataToEmit.uuid = this.selectedDish.uuid;
            }
            
            this.save.emit(dataToEmit);
            this.closeModal();
        } else {
            this.notificationService.error('Por favor completa todos los campos requeridos');
        }
    }

    closeModal() {
        this.resetForm();
        this.selectedDish = null;
        this.close.emit();
    }
}
