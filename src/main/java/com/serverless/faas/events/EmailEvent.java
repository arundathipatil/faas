package com.serverless.faas.events;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import java.util.UUID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Iterator;

public class EmailEvent implements RequestHandler<SNSEvent, Object> {
    private DynamoDB dynamoDB;
    private static String SENDERS_EMAIL = System.getenv("SendersEmail");
    private static final String EMAIL_SUBJECT="Reset Password";

    public EmailEvent() {
        AmazonDynamoDBClient dynamoClient = new AmazonDynamoDBClient();
        dynamoClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        this.dynamoDB = new DynamoDB(dynamoClient);
    }

    @Override
    public Object handleRequest(SNSEvent snsEvent, Context context) {
        context.getLogger().log("inside lambda function...!!!!!!!!");

        if(snsEvent.getRecords() == null) {
            context.getLogger().log("There are no events available");
            return null;
        }

        if (dynamoDB == null) {
            context.getLogger().log("Dynamo db object is null");
        }
        TableCollection<ListTablesResult> dbTables = dynamoDB.listTables();
        Iterator<Table> iterator = dbTables.iterator();
        while (iterator.hasNext()) {
            Table table1 = iterator.next();
            context.getLogger().log("Dynamodb table name:- " + table1.getTableName());
        }
        Table table = dynamoDB.getTable("csye6225");

        if (table == null)
            context.getLogger().log("Table not present in dynamoDB");

        String messageFromSQS =  snsEvent.getRecords().get(0).getSNS().getMessage();
        String email = messageFromSQS.split(",")[0];
        context.getLogger().log("Sending email to "+ email);
        String token = messageFromSQS.split(",")[1];
        context.getLogger().log("Token: " + token + "token=========");

        Item item = dynamoDB.getTable("csye6225").getItem("id", email);
        if(item == null) {

        }

        // http://example.com/reset?email=user@somedomain.com&token=4e163b8b-889a-4ce7-a3f7-61041e323c23
        long ttlTime = Instant.now().getEpochSecond() + 15*60;
        if ((item != null && Long.parseLong(item.get("TTL").toString()) < Instant.now().getEpochSecond() || item == null)) {
//            String token = UUID.randomUUID().toString();
            Item itemToAdd= new Item().withString("id", email).withLong("TTL", ttlTime);
            PutItemSpec item2 = new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("id", email)
                    .withString("token", token)
                    .withLong("ttl", ttlTime));
            dynamoDB.getTable("csye6225").putItem(item2);
            String link = "http://prod.arundathipatil.com/reset?email="+email+"&token="+token;

            context.getLogger().log("AWS request ID:" + context.getAwsRequestId());
            context.getLogger().log("AWS message ID:" + snsEvent.getRecords().get(0).getSNS().getMessageId());

            Content content = new Content().withData(link);
            Body body = new Body().withText(content);
            try {
                if (SENDERS_EMAIL == null) {
                    SENDERS_EMAIL = "donotreply@prod.arundathipatil.me";
                }
                AmazonSimpleEmailService client =
                        AmazonSimpleEmailServiceClientBuilder.standard()
                                .withRegion(Regions.US_EAST_1).build();
                SendEmailRequest emailRequest = new SendEmailRequest()
                        .withDestination(
                                new Destination().withToAddresses(email))
                        .withMessage(new Message()
                                .withBody(body)
                                .withSubject(new Content()
                                        .withCharset("UTF-8").withData(EMAIL_SUBJECT)))
                        .withSource(SENDERS_EMAIL);
                client.sendEmail(emailRequest);
                context.getLogger().log("Email Request sent is : " + emailRequest.toString() + "---");
                context.getLogger().log("Email sent to "+ email + " successfully!");
            } catch (Exception ex) {
                context.getLogger().log(ex.getMessage());
            }

        }
        return null;
    }
}


