FROM maven:3.9.11-eclipse-temurin-17

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:99
ENV SCREEN_WIDTH=1920
ENV SCREEN_HEIGHT=1080
ENV SCREEN_DEPTH=24

RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    chromium-driver \
    xvfb \
    fluxbox \
    x11vnc \
    novnc \
    websockify \
    x11-utils \
    wget \
    unzip \
    ca-certificates \
    fonts-liberation \
    libnss3 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    libgbm1 \
    libasound2 \
    procps \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY . /workspace
COPY docker/entrypoint.sh /usr/local/bin/project-entrypoint.sh

RUN chmod +x /usr/local/bin/project-entrypoint.sh

EXPOSE 5900 6080

ENTRYPOINT ["/usr/local/bin/project-entrypoint.sh"]
