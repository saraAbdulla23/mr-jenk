# Use official Jenkins inbound agent as base
FROM jenkins/inbound-agent:latest

USER root

# Install Firefox, Geckodriver, and necessary utilities
RUN apt-get update && apt-get install -y --no-install-recommends \
    firefox-esr \
    wget \
    unzip \
    ca-certificates \
    curl \
    gnupg \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install latest Geckodriver
ARG GECKODRIVER_VERSION=v0.34.0
RUN wget -qO /tmp/geckodriver.tar.gz https://github.com/mozilla/geckodriver/releases/download/${GECKODRIVER_VERSION}/geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz \
    && tar -xzf /tmp/geckodriver.tar.gz -C /usr/local/bin \
    && chmod +x /usr/local/bin/geckodriver \
    && rm /tmp/geckodriver.tar.gz

# Verify installations
RUN firefox --version && geckodriver --version

# Switch back to Jenkins user
USER jenkins

# Optional: set npm cache (if needed in the future)
ENV NPM_CONFIG_CACHE=/home/jenkins/.npm

# Optional: create directories for builds
RUN mkdir -p /home/jenkins/workspace /home/jenkins/deploy

# Set working directory
WORKDIR /home/jenkins
