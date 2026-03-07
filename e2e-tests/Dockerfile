FROM node:22-bookworm
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY package.json ./
RUN npm install
RUN npx playwright install --with-deps chromium

COPY . .

COPY entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

EXPOSE 9323
ENTRYPOINT ["/entrypoint.sh"]
