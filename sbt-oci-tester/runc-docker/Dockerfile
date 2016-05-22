FROM ubuntu

RUN apt-get -o Acquire::ForceIPv4=true update && \
  apt-get -o Acquire::ForceIPv4=true install -y curl build-essential golang seccomp libseccomp-dev && \
  curl -L https://github.com/opencontainers/runc/archive/v0.1.1.tar.gz > /v0.1.1.tar.gz && \
  tar -xvf /v0.1.1.tar.gz -C /usr/src && \
  cd /usr/src/runc-0.1.1 && export GOPATH=/usr/lib/go/ && make

CMD ["/bin/bash"]

