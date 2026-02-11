# ----------------------------
# Stage 1: Build Angular app
# ----------------------------
    FROM node:20-alpine AS build

    WORKDIR /app
    
    # Copy package files and install dependencies
    COPY package*.json ./
    RUN npm install
    
    # Copy source code
    COPY . .
    
    # Build production app into /app/dist/front/browser
    RUN npm run build -- --configuration production --output-path=dist/front
    
    # ----------------------------
    # Stage 2: Serve with Nginx
    # ----------------------------
    FROM nginx:alpine
    
    # Remove default Nginx content
    RUN rm -rf /usr/share/nginx/html/*
    
    # Copy Angular build into Nginx html
    COPY --from=build /app/dist/front/browser /usr/share/nginx/html
    
    # Rename index.csr.html → index.html so Nginx can serve it
    RUN mv /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html
    
    # Copy environment script if needed
    COPY ./docker/env.js /usr/share/nginx/html/env.js
    
    # Ensure Nginx can read files
    RUN chmod -R 755 /usr/share/nginx/html
    
    # Expose port 80
    EXPOSE 80
    
    # Start Nginx
    CMD ["nginx", "-g", "daemon off;"]
    