export interface KustomMobileSDKError {
  readonly isFatal: boolean;
  readonly message: string;
  readonly name: string;
  readonly sessionId?: string;
  readonly params?: { [key: string]: any };
}
