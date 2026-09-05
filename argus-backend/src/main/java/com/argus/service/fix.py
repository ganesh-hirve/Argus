import re

with open('C:/Users/Ganesh/Downloads/argus/argus-backend/src/main/java/com/argus/service/EnforcementService.java', 'r') as f:
    content = f.read()

def clean_args(match):
    prefix = match.group(1)
    func = match.group(2)
    args_str = match.group(3)
    
    parts = []
    current = ''
    paren_count = 0
    in_quote = False
    
    for char in args_str:
        if char == '"':
            in_quote = not in_quote
        elif char == '(' and not in_quote:
            paren_count += 1
        elif char == ')' and not in_quote:
            paren_count -= 1
        elif char == ',' and not in_quote and paren_count == 0:
            parts.append(current)
            current = ''
            continue
        current += char
    parts.append(current)
    
    parts = [p.strip() for p in parts]
    
    if len(parts) >= 11:
        new_args = f'{parts[0]}, {parts[1]}, {parts[2]}, ctx'
        return f'{prefix}{func}({new_args})'
    
    return match.group(0)

content = re.sub(r'(return\s+)(blocked|allowed)\(([^;]+)\)', clean_args, content)

if 'ValidationContext ctx = new ValidationContext();' not in content:
    content = content.replace(
        'private ExecutionResponse validateExecution(\n                        ExecutionRequest request) {',
        'private ExecutionResponse validateExecution(\n                        ExecutionRequest request) {\n\n                ValidationContext ctx = new ValidationContext();'
    )
    
    content = content.replace(
        'if (taskOptional.isEmpty()) {',
        'ctx.setTaskExists(CheckStatus.FAIL);\n                if (taskOptional.isEmpty()) {'
    )
    content = content.replace(
        'TaskAuthority task = taskOptional.get();',
        'ctx.setTaskExists(CheckStatus.PASS);\n\n                TaskAuthority task = taskOptional.get();'
    )
    
    content = content.replace(
        'if (!authorizedAgent) {',
        'ctx.setAgentAuthorization(CheckStatus.FAIL);\n                if (!authorizedAgent) {'
    )
    content = content.replace(
        'if (task.getStatus() != TaskStatus.ACTIVE) {',
        'ctx.setAgentAuthorization(CheckStatus.PASS);\n\n                // -----------------------------------------------------\n                // 4. TASK STATUS\n                // -----------------------------------------------------\n\n                if (task.getStatus() != TaskStatus.ACTIVE) {'
    )
    
    content = content.replace(
        'if (expired) {',
        'ctx.setExpiry(CheckStatus.FAIL);\n                if (expired) {'
    )
    content = content.replace(
        'Optional<TaskResource> resourceOptional = taskResourceRepository',
        'ctx.setExpiry(CheckStatus.PASS);\n\n                // -----------------------------------------------------\n                // 6. RESOURCE AUTHORIZATION\n                // -----------------------------------------------------\n\n                Optional<TaskResource> resourceOptional = taskResourceRepository'
    )
    
    content = content.replace(
        'if (resourceOptional.isEmpty()) {',
        'ctx.setResourceAuthorization(CheckStatus.FAIL);\n                if (resourceOptional.isEmpty()) {'
    )
    content = content.replace(
        'TaskResource resource = resourceOptional.get();',
        'ctx.setResourceAuthorization(CheckStatus.PASS);\n\n                TaskResource resource = resourceOptional.get();'
    )
    
    content = content.replace(
        'if (duplicate) {',
        'ctx.setDuplicateExecution(CheckStatus.FAIL);\n                if (duplicate) {'
    )
    content = content.replace(
        'boolean validStateTransition = isActionAllowedForState(',
        'ctx.setDuplicateExecution(CheckStatus.PASS);\n\n                // -----------------------------------------------------\n                // 8. STATE TRANSITION\n                // -----------------------------------------------------\n\n                boolean validStateTransition = isActionAllowedForState('
    )
    
    content = content.replace(
        'if (!validStateTransition) {',
        'ctx.setStateTransition(CheckStatus.FAIL);\n                if (!validStateTransition) {'
    )
    content = content.replace(
        'if (request.getAction() == ActionType.PAY) {',
        'ctx.setStateTransition(CheckStatus.PASS);\n\n                // -----------------------------------------------------\n                // 9. PAYMENT VALIDATION\n                // -----------------------------------------------------\n\n                if (request.getAction() == ActionType.PAY) {'
    )
    
    content = content.replace(
        'if (!request.getAmount()\n                                        .equals(resource.getAmount())) {',
        'ctx.setAmountIntegrity(CheckStatus.FAIL);\n                        if (!request.getAmount()\n                                        .equals(resource.getAmount())) {'
    )
    content = content.replace(
        'if (request.getAmount() > task.getAvailableBudget()) {',
        'ctx.setAmountIntegrity(CheckStatus.PASS);\n                        ctx.setBudgetAvailability(CheckStatus.FAIL);\n                        if (request.getAmount() > task.getAvailableBudget()) {'
    )
    content = content.replace(
        'return allowed(\n                                "Execution authorized",',
        'ctx.setBudgetAvailability(CheckStatus.PASS);\n                ctx.setAmountIntegrity(CheckStatus.PASS);\n\n                return allowed(\n                                "Execution authorized",'
    )

    execute_ctx = '''public ExecutionResponse execute(ExecutionRequest request) {

                ValidationContext ctx = new ValidationContext();
                ctx.setTaskExists(CheckStatus.PASS);
                ctx.setAgentAuthorization(CheckStatus.PASS);
                ctx.setResourceAuthorization(CheckStatus.PASS);
                ctx.setStateTransition(CheckStatus.PASS);
                ctx.setBudgetAvailability(CheckStatus.PASS);
                ctx.setExpiry(CheckStatus.PASS);
                ctx.setDuplicateExecution(CheckStatus.PASS);
                ctx.setAmountIntegrity(CheckStatus.PASS);
'''
    content = content.replace('public ExecutionResponse execute(ExecutionRequest request) {', execute_ctx)

with open('C:/Users/Ganesh/Downloads/argus/argus-backend/src/main/java/com/argus/service/EnforcementService.java', 'w') as f:
    f.write(content)
