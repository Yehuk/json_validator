FROM maven:3.3-jdk-8-onbuild
CMD ["java","-jar","/usr/src/app/target/validator-0.1-jar-with-dependencies.jar"]
EXPOSE 80
#  sudo docker build -t validator .
#  sudo docker run -d --rm -p 80:80  validator
