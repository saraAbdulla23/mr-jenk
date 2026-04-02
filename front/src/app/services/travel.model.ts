export interface Travel {
    id?: number;
    startDate: string;
    endDate: string;
    duration: number;
    accommodation: string;
    transportation: string;
    destinations: string[];
    activities: string[];
  }