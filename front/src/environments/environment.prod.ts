export const environment = {
    production: true,
    apiGatewayUrl: (window as any)['env']?.VITE_API_GATEWAY_URL || 'http://localhost:8080'
  };
  