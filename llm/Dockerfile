FROM gradle AS build

COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle installDist --no-daemon

FROM eclipse-temurin:21

RUN mkdir /app
COPY --from=build /src/app/build/install/app /app/
WORKDIR /app

RUN mkdir targets
COPY targets/pythonpass targets/pythonpass

RUN apt update && apt install -y python3 python3-dev python3.12-venv gcc
RUN python3 -mvenv ~/.virtualenvs/cpg
RUN ~/.virtualenvs/cpg/bin/pip3 install jep

ENTRYPOINT ["./bin/app", "--daemon"]