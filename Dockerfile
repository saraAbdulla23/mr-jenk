# =========================
# Jenkins Inbound Agent + Firefox + Geckodriver + Docker + Node + Maven
# =========================
FROM jenkins/inbound-agent:latest

USER root

# -------------------------
# Install system utilities
# -------------------------
RUN apt-get update && apt-get install -y --no-install-recommends \
    firefox-esr \
    wget \
    unzip \
    ca-certificates \
    curl \
    gnupg \
    git \
    docker.io \
    maven \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

# -------------------------
# Install latest Geckodriver
# -------------------------
ARG GECKODRIVER_VERSION=v0.34.0
RUN wget -qO /tmp/geckodriver.tar.gz \
      https://github.com/mozilla/geckodriver/releases/download/${GECKODRIVER_VERSION}/geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz \
    && tar -xzf /tmp/geckodriver.tar.gz -C /usr/local/bin \
    && chmod +x /usr/local/bin/geckodriver \
    && rm /tmp/geckodriver.tar.gz

# -------------------------
# Verify installations
# -------------------------
RUN firefox --version \
    && geckodriver --version \
    && docker --version \
    && node -v \
    && npm -v \
    && mvn -v \
    && git --version

# -------------------------
# Create Jenkins directories & npm cache
# -------------------------
RUN mkdir -p /home/jenkins/workspace \
    /home/jenkins/deploy \
    /home/jenkins/.npm

# -------------------------
# Switch back to Jenkins user
# -------------------------
USER jenkins

# -------------------------
# Environment variables
# -------------------------
ENV NPM_CONFIG_CACHE=/home/jenkins/.npm
WORKDIR /home/jenkins

# -------------------------
# Optional: add Docker group permissions
# -------------------------
# This allows running Docker commands without root inside the container
USER root
RUN groupadd -g 999 docker || true \
    && usermod -aG docker jenkins
USER jenkins
