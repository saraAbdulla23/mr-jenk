# ===============================
# Stage 1 - Build
# ===============================
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build

# ===============================
# Stage 2 - Runtime
# ===============================
FROM node:20-alpine

WORKDIR /app

# Install only production deps
COPY package*.json ./
RUN npm install --omit=dev

# Copy built files
COPY --from=builder /app/dist ./dist

EXPOSE 4000

# Angular SSR entry point
CMD ["node", "dist/front/server/main.js"]
