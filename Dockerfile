FROM selenium/standalone-chrome

USER root

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:99
ENV SCREEN_WIDTH=1920
ENV SCREEN_HEIGHT=1080
ENV SCREEN_DEPTH=24
ENV MAVEN_OPTS=-Xmx1024m

RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY . /workspace
COPY docker/entrypoint.sh /usr/local/bin/project-entrypoint.sh

RUN chmod +x /usr/local/bin/project-entrypoint.sh \
    && sed -i 's/\r$//' /usr/local/bin/project-entrypoint.sh \
    && chown -R seluser:seluser /workspace

EXPOSE 5900 6080

ENTRYPOINT ["bash", "/usr/local/bin/project-entrypoint.sh"]
