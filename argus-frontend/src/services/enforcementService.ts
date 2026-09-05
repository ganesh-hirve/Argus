import type { ApiExecutionRequest, Evaluation } from '../types/execution'

async function post(path: string, request: ApiExecutionRequest): Promise<Evaluation> {
  const response = await fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) })
  if (!response.ok) throw new Error(`Authorization service returned HTTP ${response.status}`)
  return response.json() as Promise<Evaluation>
}

/** Swap this transport if the deployed API lives on a different origin. */
export const evaluateRequest = (request: ApiExecutionRequest) => post('/api/argus/evaluate', request)
export const executeRequest = (request: ApiExecutionRequest) => post('/api/argus/execute', request)
