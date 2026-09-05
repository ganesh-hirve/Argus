export type Action = '' | 'VERIFY' | 'PAY'
export type Decision = 'ALLOWED' | 'BLOCKED'
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export interface ExecutionRequest { taskId: string; agentId: string; resourceId: string; action: Action; amount: string; idempotencyKey: string }
export interface ApiExecutionRequest { taskId: string; agentId: string; resourceId: string; action: Exclude<Action, ''>; amount: number | null; idempotencyKey: string }
export interface Evaluation { decision: Decision; reason: string; taskId: string; resourceId: string; availableBudget: number; riskAssessment: { riskScore: number; riskLevel: RiskLevel; signals: string[] } }
