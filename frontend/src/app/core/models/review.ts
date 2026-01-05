export interface Review {
    uuid: string;
    user: {
        username: string;
    };
    comment: string;
    rating: number;
    dateCreated: string;
}

export interface ReviewStats {
    averageRating: number;
    totalReviews: number;
    ratings: {
        1: number;
        2: number;
        3: number;
        4: number;
        5: number;
    };
}

export interface ReviewResponse {
    success: boolean;
    message: string;
    dishName: string;
    dishUuid: string;
    reviews: Review[];
}

export interface ReviewStatsResponse {
    success: boolean;
    message: string;
    stats: ReviewStats;
}

export interface CreateReviewRequest {
    dishId: number;
    rating: number;
    comment?: string;
}

export interface CreateReviewResponse {
    success: boolean;
    message: string;
    review: Review;
}