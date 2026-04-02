import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';

import { TravelService } from '../services/travel.service';
import { Travel } from '../services/travel.model';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-manage-travel',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule
  ],
  templateUrl: './manage-travel.html',
  styleUrls: ['./manage-travel.scss']
})
export class ManageTravel implements OnInit {

  travels: Travel[] = [];
  loading = false;
  errorMessage = '';
  selectedTravel: Travel | null = null;

  form: FormGroup;

  constructor(
    private travelService: TravelService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      duration: [0, Validators.required],
      accommodation: ['', Validators.required],
      transportation: ['', Validators.required],
      destinations: ['', Validators.required], // comma-separated
      activities: ['', Validators.required]    // comma-separated
    });
  }

  ngOnInit(): void {
    this.fetchTravels();
  }

  // ================= FETCH =================
  fetchTravels(): void {
    this.loading = true;
    this.errorMessage = '';
    this.travelService.getAllTravels()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: travels => this.travels = travels,
        error: () => this.errorMessage = 'Failed to fetch travels'
      });
  }

  // ================= SELECT =================
  selectTravel(travel: Travel): void {
    this.selectedTravel = travel;

    this.form.patchValue({
      startDate: travel.startDate,
      endDate: travel.endDate,
      duration: travel.duration,
      accommodation: travel.accommodation,
      transportation: travel.transportation,
      destinations: travel.destinations.join(', '),
      activities: travel.activities.join(', ')
    });
  }

  // ================= CLEAR =================
  clearSelection(): void {
    this.selectedTravel = null;
    this.form.reset({
      startDate: '',
      endDate: '',
      duration: 0,
      accommodation: '',
      transportation: '',
      destinations: '',
      activities: ''
    });
  }

  // ================= SAVE =================
  saveTravel(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.value;
    const travelData: Travel = {
      startDate: formValue.startDate,
      endDate: formValue.endDate,
      duration: formValue.duration,
      accommodation: formValue.accommodation,
      transportation: formValue.transportation,
      destinations: formValue.destinations.split(',').map((d: string) => d.trim()),
      activities: formValue.activities.split(',').map((a: string) => a.trim())
    };

    if (this.selectedTravel) {
      // Update
      this.travelService.updateTravel(this.selectedTravel.id as number, travelData)
        .subscribe({
          next: () => {
            this.fetchTravels();
            this.clearSelection();
          },
          error: () => this.errorMessage = 'Failed to update travel'
        });
    } else {
      // Create
      this.travelService.createTravel(travelData)
        .subscribe({
          next: () => {
            this.fetchTravels();
            this.clearSelection();
          },
          error: () => this.errorMessage = 'Failed to create travel'
        });
    }
  }

  // ================= DELETE =================
  deleteTravel(travel: Travel): void {
    if (!confirm(`Delete travel from ${travel.startDate} to ${travel.endDate}?`)) return;

    this.travelService.deleteTravel(travel.id as number).subscribe({
      next: () => this.fetchTravels(),
      error: () => this.errorMessage = 'Failed to delete travel'
    });
  }
}