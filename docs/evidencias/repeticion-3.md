# Repetición 3: ejecución real

Fecha UTC: 2026-11-19T21:35:28.590784+00:00

Mismos upstreams, misma configuración, ninguna variación opcional.

Estado inicial: **EN PROCESO**.

```text
$ docker compose --profile expansion down -v --remove-orphans
 Container kata5-primes-1-1 Stopping 
 Container kata5-primes-2-1 Stopping 
 Container kata5-nginx-1 Stopping 
 Container kata5-fibonacci-3-1 Stopping 
 Container kata5-fibonacci-1-1 Stopping 
 Container kata5-fibonacci-2-1 Stopping 
 Container kata5-fibonacci-3-1 Stopped 
 Container kata5-fibonacci-3-1 Removing 
 Container kata5-fibonacci-3-1 Removed 
 Container kata5-fibonacci-2-1 Stopped 
 Container kata5-fibonacci-2-1 Removing 
 Container kata5-fibonacci-2-1 Removed 
 Container kata5-fibonacci-1-1 Stopped 
 Container kata5-fibonacci-1-1 Removing 
 Container kata5-fibonacci-1-1 Removed 
 Container kata5-primes-1-1 Stopped 
 Container kata5-primes-1-1 Removing 
 Container kata5-primes-1-1 Removed 
 Container kata5-primes-2-1 Stopped 
 Container kata5-primes-2-1 Removing 
 Container kata5-primes-2-1 Removed 
 Container kata5-nginx-1 Stopped 
 Container kata5-nginx-1 Removing 
 Container kata5-nginx-1 Removed 
 Container kata5-consul-1 Stopping 
 Container kata5-consul-1 Stopped 
 Container kata5-consul-1 Removing 
 Container kata5-consul-1 Removed 
 Network kata5_kata5 Removing 
 Network kata5_kata5 Removed 
$ docker compose up -d --build --wait
 Image kata5-service:local Building 
 Image kata5-service:local Building 
 Image kata5-service:local Building 
 Image kata5-service:local Building 
#1 [internal] load local bake definitions
#1 reading from stdin 2.10kB 0.0s done
#1 DONE 0.0s

#2 [primes-2 internal] load build definition from Dockerfile
#2 DONE 0.0s

#2 [primes-2 internal] load build definition from Dockerfile
#2 transferring dockerfile: 453B done
#2 DONE 0.0s

#3 [primes-2 internal] load metadata for docker.io/library/eclipse-temurin:21-jre
#3 DONE 0.6s

#4 [primes-1 internal] load metadata for docker.io/library/maven:3.9.9-eclipse-temurin-21
#4 DONE 0.6s

#5 [fibonacci-1 internal] load .dockerignore
#5 transferring context: 112B done
#5 DONE 0.0s

#6 [fibonacci-1 build 1/7] FROM docker.io/library/maven:3.9.9-eclipse-temurin-21@sha256:3a4ab3276a087bf276f79cae96b1af04f53731bec53fb2e651aca79e4b10211e
#6 resolve docker.io/library/maven:3.9.9-eclipse-temurin-21@sha256:3a4ab3276a087bf276f79cae96b1af04f53731bec53fb2e651aca79e4b10211e 0.0s done
#6 DONE 0.0s

#7 [primes-2 stage-1 1/3] FROM docker.io/library/eclipse-temurin:21-jre@sha256:ca73b4d3961703d6d7830a4361f49ef0478f8f28b39defc07bb72f4cc7f834f6
#7 resolve docker.io/library/eclipse-temurin:21-jre@sha256:ca73b4d3961703d6d7830a4361f49ef0478f8f28b39defc07bb72f4cc7f834f6 0.0s done
#7 DONE 0.0s

#8 [primes-2 internal] load build context
#8 transferring context: 2.17kB done
#8 DONE 0.0s

#9 [primes-2 build 4/7] COPY service/pom.xml service/pom.xml
#9 CACHED

#10 [primes-2 build 7/7] RUN mvn -B -ntp -pl service -am -DskipTests package
#10 CACHED

#11 [primes-2 build 2/7] WORKDIR /workspace
#11 CACHED

#12 [primes-2 build 5/7] COPY client/pom.xml client/pom.xml
#12 CACHED

#13 [primes-2 stage-1 2/3] WORKDIR /app
#13 CACHED

#14 [primes-2 build 3/7] COPY pom.xml ./
#14 CACHED

#15 [primes-2 build 6/7] COPY service/src service/src
#15 CACHED

#16 [primes-1 stage-1 3/3] COPY --from=build /workspace/service/target/service-1.0.0.jar /app/service.jar
#16 CACHED

#17 [primes-2] exporting to image
#17 exporting layers done
#17 exporting manifest sha256:59a9f1d338c563ec7eb87e87dfeca0e39e1f0cf43d721e5c29c1afb3df44f0d6 done
#17 exporting config sha256:a671a6e550fdf34a90482ac5740cdcd68e15b8b3a787907c00612123ae19c375
#17 exporting config sha256:a671a6e550fdf34a90482ac5740cdcd68e15b8b3a787907c00612123ae19c375 done
#17 exporting attestation manifest sha256:19d0886a5293e64bec33e62c16c37c9873c2fc049b4fd7f086a0f070d617072f
#17 exporting attestation manifest sha256:19d0886a5293e64bec33e62c16c37c9873c2fc049b4fd7f086a0f070d617072f 0.1s done
#17 exporting manifest list sha256:a1bf03515138c230a1582e16038e201de60dcfb3a27d0a8105d445d0f01291dc
#17 exporting manifest list sha256:a1bf03515138c230a1582e16038e201de60dcfb3a27d0a8105d445d0f01291dc 0.1s done
#17 naming to docker.io/library/kata5-service:local 0.0s done
#17 unpacking to docker.io/library/kata5-service:local 0.0s done
#17 DONE 0.3s

#18 [fibonacci-1] exporting to image
#18 exporting layers done
#18 exporting manifest sha256:88c3c0c543c8b4526521ca7bd68e7bc5b43cb1207c6b5cde96c477424df2fc32 done
#18 exporting config sha256:81882195264b792a1802d2e62fc0079a3366da0697ec403212eee60146ff3ac1 0.0s done
#18 exporting attestation manifest sha256:4aec3778f2e64ad284b1136ce3dad85cd0ca0c4d8f8fa24c7fea771b79ada4ae 0.1s done
#18 exporting manifest list sha256:dba1ce996c4f6e93232ddb239b5574d7a7bfb9c13b37511a9fde29199a48bf27 0.0s done
#18 naming to docker.io/library/kata5-service:local 0.0s done
#18 unpacking to docker.io/library/kata5-service:local 0.0s done
#18 DONE 0.3s

#19 [primes-1] exporting to image
#19 exporting layers done
#19 exporting manifest sha256:263d7ae1880658c04c59a9441e16c65a442b5965a511e5a37c4cd2b67fe1138d 0.0s done
#19 exporting config sha256:5d19035418039f8bd3c609c2124d2a1deea3f149a437d08eb8de84eab04e14ee 0.0s done
#19 exporting attestation manifest sha256:5a82e18c9c41362631fdfea9451d5680fca02e0447e7fadd6f1fb946dffeb905 0.1s done
#19 exporting manifest list sha256:035b5cee359aac5d08d91ec53c62b404d2efa33ae7f0d7d1a66bcb946d4302a3 0.0s done
#19 naming to docker.io/library/kata5-service:local 0.0s done
#19 unpacking to docker.io/library/kata5-service:local 0.0s done
#19 DONE 0.3s

#20 [fibonacci-2] exporting to image
#20 exporting layers done
#20 exporting manifest sha256:773a18aaba8eda563e694c60d9d41d53e7d56517f86a81543ffdd8666d85740c 0.0s done
#20 exporting config sha256:eba3c28ae2ed1ffd36d3e90b94bd788297929fe7318fa0367d9220de039daf9f 0.0s done
#20 exporting attestation manifest sha256:a296043bd08e3058a2535fd719d15eded341e751067a7544681eb8653b0d1f9a 0.1s done
#20 exporting manifest list sha256:a105776a0f2cecafc9c8c1ecacf245a8d25006324414f79ac7ab653df65f0f9a 0.0s done
#20 naming to docker.io/library/kata5-service:local done
#20 unpacking to docker.io/library/kata5-service:local 0.0s done
#20 DONE 0.4s

#21 [primes-2] resolving provenance for metadata file
#21 DONE 0.1s

#22 [fibonacci-2] resolving provenance for metadata file
#22 DONE 0.1s

#23 [fibonacci-1] resolving provenance for metadata file
#23 DONE 0.1s

#24 [primes-1] resolving provenance for metadata file
#24 DONE 0.0s
 Image kata5-service:local Built 
 Image kata5-service:local Built 
 Image kata5-service:local Built 
 Image kata5-service:local Built 
 Network kata5_kata5 Creating 
 Network kata5_kata5 Created 
 Container kata5-consul-1 Creating 
 Container kata5-consul-1 Created 
 Container kata5-primes-2-1 Creating 
 Container kata5-primes-1-1 Creating 
 Container kata5-nginx-1 Creating 
 Container kata5-fibonacci-2-1 Creating 
 Container kata5-fibonacci-1-1 Creating 
 Container kata5-primes-1-1 Created 
 Container kata5-primes-2-1 Created 
 Container kata5-fibonacci-2-1 Created 
 Container kata5-fibonacci-1-1 Created 
 Container kata5-nginx-1 Created 
 Container kata5-consul-1 Starting 
 Container kata5-consul-1 Started 
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Healthy 
 Container kata5-primes-1-1 Starting 
 Container kata5-consul-1 Healthy 
 Container kata5-fibonacci-2-1 Starting 
 Container kata5-consul-1 Healthy 
 Container kata5-primes-2-1 Starting 
 Container kata5-consul-1 Healthy 
 Container kata5-fibonacci-1-1 Starting 
 Container kata5-consul-1 Healthy 
 Container kata5-nginx-1 Starting 
 Container kata5-primes-2-1 Started 
 Container kata5-nginx-1 Started 
 Container kata5-primes-1-1 Started 
 Container kata5-fibonacci-1-1 Started 
 Container kata5-fibonacci-2-1 Started 
 Container kata5-fibonacci-2-1 Waiting 
 Container kata5-primes-1-1 Waiting 
 Container kata5-primes-2-1 Waiting 
 Container kata5-consul-1 Waiting 
 Container kata5-nginx-1 Waiting 
 Container kata5-fibonacci-1-1 Waiting 
 Container kata5-fibonacci-1-1 Healthy 
 Container kata5-consul-1 Healthy 
 Container kata5-primes-1-1 Healthy 
 Container kata5-fibonacci-2-1 Healthy 
 Container kata5-primes-2-1 Healthy 
 Container kata5-nginx-1 Healthy 
$ docker compose ps
NAME                  IMAGE                     COMMAND                  SERVICE       CREATED         STATUS                   PORTS
kata5-consul-1        hashicorp/consul:1.20.3   "docker-entrypoint.sâ€¦"   consul        8 seconds ago   Up 7 seconds (healthy)   127.0.0.1:8500->8500/tcp, 127.0.0.1:8600->8600/udp
kata5-fibonacci-1-1   kata5-service:local       "java -jar /app/servâ€¦"   fibonacci-1   7 seconds ago   Up 3 seconds             8080/tcp
kata5-fibonacci-2-1   kata5-service:local       "java -jar /app/servâ€¦"   fibonacci-2   7 seconds ago   Up 3 seconds             8080/tcp
kata5-nginx-1         nginx:1.28.0-alpine       "/docker-entrypoint.â€¦"   nginx         7 seconds ago   Up 3 seconds (healthy)   127.0.0.1:8080->80/tcp
kata5-primes-1-1      kata5-service:local       "java -jar /app/servâ€¦"   primes-1      7 seconds ago   Up 3 seconds             8080/tcp
kata5-primes-2-1      kata5-service:local       "java -jar /app/servâ€¦"   primes-2      7 seconds ago   Up 3 seconds             8080/tcp
$ docker compose exec -T nginx nginx -t
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
$ docker compose exec -T nginx nginx -v
nginx version: nginx/1.28.0
$ docker compose ps -q nginx
93d0ecbc836d09ff3f8114d5cfdb2b1689ab0163611bc09e17fe660425d83018
$ C:\Users\enrif\AppData\Local\Programs\Python\Python313\python.exe scripts/verify.py --scenario baseline --base http://127.0.0.1:8080
{
  "counts": {
    "fibonacci": {
      "fibonacci-1": 12,
      "fibonacci-2": 12
    },
    "primes": {
      "primes-1": 12,
      "primes-2": 12
    }
  },
  "result": "PASS",
  "scenario": "baseline"
}
$ docker compose stop fibonacci-2
 Container kata5-fibonacci-2-1 Stopping 
 Container kata5-fibonacci-2-1 Stopped 
$ C:\Users\enrif\AppData\Local\Programs\Python\Python313\python.exe scripts/verify.py --scenario down --base http://127.0.0.1:8080
{
  "counts": {
    "fibonacci": {
      "fibonacci-1": 24
    },
    "primes": {
      "primes-1": 12,
      "primes-2": 12
    }
  },
  "result": "PASS",
  "scenario": "down"
}
$ docker compose ps -q nginx
93d0ecbc836d09ff3f8114d5cfdb2b1689ab0163611bc09e17fe660425d83018
$ docker compose --profile expansion up -d fibonacci-3
 Container kata5-primes-2-1 Running 
 Container kata5-primes-1-1 Running 
 Container kata5-consul-1 Running 
 Container kata5-nginx-1 Running 
 Container kata5-fibonacci-1-1 Running 
 Container kata5-fibonacci-3-1 Creating 
 Container kata5-fibonacci-3-1 Created 
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Healthy 
 Container kata5-fibonacci-3-1 Starting 
 Container kata5-fibonacci-3-1 Started 
$ C:\Users\enrif\AppData\Local\Programs\Python\Python313\python.exe scripts/verify.py --scenario up --base http://127.0.0.1:8080
  espera de salud/DNS (up), intento 1: fibonacci: se esperaban réplicas ['fibonacci-1', 'fibonacci-3'] y respondieron ['fibonacci-1']
{
  "counts": {
    "fibonacci": {
      "fibonacci-1": 12,
      "fibonacci-3": 12
    },
    "primes": {
      "primes-1": 12,
      "primes-2": 12
    }
  },
  "result": "PASS",
  "scenario": "up"
}
$ docker compose ps -q nginx
93d0ecbc836d09ff3f8114d5cfdb2b1689ab0163611bc09e17fe660425d83018
$ docker compose --profile expansion stop fibonacci-3
 Container kata5-fibonacci-3-1 Stopping 
 Container kata5-fibonacci-3-1 Stopped 
$ docker compose start fibonacci-2
 Container kata5-consul-1 Waiting 
 Container kata5-consul-1 Healthy 
 Container kata5-fibonacci-2-1 Starting 
 Container kata5-fibonacci-2-1 Started 
$ C:\Users\enrif\AppData\Local\Programs\Python\Python313\python.exe scripts/verify.py --scenario baseline --base http://127.0.0.1:8080
  espera de salud/DNS (baseline), intento 1: fibonacci: se esperaban réplicas ['fibonacci-1', 'fibonacci-2'] y respondieron ['fibonacci-1']
{
  "counts": {
    "fibonacci": {
      "fibonacci-1": 12,
      "fibonacci-2": 12
    },
    "primes": {
      "primes-1": 12,
      "primes-2": 12
    }
  },
  "result": "PASS",
  "scenario": "baseline"
}
$ docker compose ps -q nginx
93d0ecbc836d09ff3f8114d5cfdb2b1689ab0163611bc09e17fe660425d83018

```

**PASS**: comprobados reparto, baja, alta, independencia de upstreams, consistencia y ausencia de reinicios de NGINX.
