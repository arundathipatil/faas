# faas
This repository holds serverless lambda functions


## Getting Started

### Prerequisites..
	JDK 8 or later
	Maven 3.0 or later
	Git



## Clone

### To get started you can simply clone this repository using git:

	git clone git@github.com:arundathipatil/faas.git
	

## Build and run locally
### To build and run the project locally use the below commands
1. mvn clean instal 
2. mvn spring-boot:run


## Build

1. Create lambda function either through terraform or AWS console and upload a dummy zip/jar file
2. Run 'mvn clean install'. he build artifacts will be stored in the `target/` directory.
3. update the lambda function. Run `aws lambda update-function-code --function-name  emailOnSNS  --s3-bucket ${BUCKET_NAME} --s3-key faas-0.0.1-SNAPSHOT.jar --region ${AWS_REGION}` from root directory. The build artifacts will be uploaded to s3 bucket.
4. Invoke the lambda function from webapplication using AWS SDK libraries

## Running unit tests

Run `mvn clean test` to execute the unit tests.


## Webapp
1. Download AWS SDK dependenices for AWS SNS, SQS, SES
2. On evry password reset request by user , a message is addedd to SQS and SNS topic is trigger which executed the lambda function


## Clone

### To get started with webapp clone this repository using git:

	git clone git@github.com:arundathipatil/webapp.git
	
## Build and run locally
1. mvn clean instal 
2. mvn spring-boot:run
