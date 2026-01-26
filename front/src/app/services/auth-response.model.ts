export interface AuthResponse {
  token: string;
  user: {
    id: string;
    email: string;
    name: string;
    role: 'CLIENT' | 'SELLER'; // restrict to two roles
    avatar?: string;
  };
}
