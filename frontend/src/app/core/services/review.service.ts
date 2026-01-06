import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class ReviewService {
    URL_BASE = 'http://localhost:3050/backend/api';

    constructor(private http: HttpClient) { }

    getReviews(dishId: number, page: number = 1, max: number = 5, rating?: number) {
        let params = new HttpParams()
            .set('dishId', dishId.toString())
            .set('page', page.toString())
            .set('max', max.toString());

        if (rating) {
            params = params.set('rating', rating.toString());
        }

        return this.http.get(`${this.URL_BASE}/review/list`, { params });
    }

    getReviewStats(dishId: number) {
        return this.http.get(`${this.URL_BASE}/review/stats/${dishId}`);
    }

    createReview(reviewData: any) {
        return this.http.post(`${this.URL_BASE}/review/new`, reviewData);
    }
}