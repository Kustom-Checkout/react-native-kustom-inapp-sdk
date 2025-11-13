export interface KustomProductEvent {
  readonly action: string;
  readonly params: { [key: string]: any };
  readonly sessionId?: string;
}
