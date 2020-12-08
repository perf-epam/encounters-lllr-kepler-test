Infrastructure deployment
=========================

## Cloudformation

### Infrastructure

**Technical debt**:
1. Custom keys for encryption/decryption of secrets
1. More granular permissions for roles
1. Add tags to resources that don't inherit ones from the CloudFormation stack
1. Use cache for CodeBuild (Docker image layers + Maven cache)


Name the stack first:
```shell
$ export STACK_NAME=hec-epam-prototype-deadpool-kepler-infra
```

Create:
```shell
$ aws cloudformation create-stack \
    --stack-name $STACK_NAME \
    --disable-rollback \
    --capabilities CAPABILITY_NAMED_IAM \
    --template-body file://infrastructure/cloudformation/infrastructure.yml \
    --parameters file://infrastructure/cloudformation/config/common.json \
    --tags Key=Product,Value=HealtheCare Key=Environment,Value=Development Key=EPAMDevelopmentTeam,Value=Deadpool
```

Subsequent updates:
```shell
$ aws cloudformation deploy \
    --stack-name $STACK_NAME \
    --no-fail-on-empty-changeset \
    --capabilities CAPABILITY_NAMED_IAM \
    --template-file infrastructure/cloudformation/infrastructure.yml \
    --tags Product=HealtheCare Environment=Development EPAMDevelopmentTeam=Deadpool
```

Go to [Secrets Manager](https://us-west-2.console.aws.amazon.com/secretsmanager/home?region=us-west-2#/listSecrets)
and find `<HadoopOauthSecretPath>` secret.

Put a valid secret token in `token` field

Add webhook for CodeBuild project:
```shell
$ aws codebuild create-webhook \
    --project-name <value> \
    --filter-groups  '[[{"type": "EVENT", "pattern": "PUSH"}, {"type": "HEAD_REF", "pattern": "^refs/heads/<GithubBranch>"}]]'
```
Note the `webhook.payloadUrl` and `webhook.secret` in the response.

Create the webhook in your Github repo using those values.


### Run application on ECS

Run a one-shot task:
```shell
$ aws ecs run-task \
    --cluster <ECSCluster> \
    --task-definition <TaskDefinitionName> \
    --count 1 \
    --enable-ecs-managed-tags \
    --launch-type FARGATE \
    --network-configuration 'awsvpcConfiguration={subnets=[<subnets>],securityGroups=[<security-groups>],assignPublicIp=DISABLED}'
```

To get values of `<different>` placeholders, use the command:
```shell
$ aws cloudformation describe-stacks \
    --stack-name hec-epam-prototype-deadpool-kepler-infra \
    --query 'Stacks[0].Outputs[].[join(\'=\', [OutputKey,OutputValue])][]'
```
