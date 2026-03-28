FROM node:18-alpine

WORKDIR /app

RUN apk add --no-cache \
    chromium \
    ca-certificates \
    ttf-freefont \
    tzdata \
    git \
    python3 \
    make \
    g++

ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser

COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .

EXPOSE 4002

CMD ["node", "src/index.js"]
