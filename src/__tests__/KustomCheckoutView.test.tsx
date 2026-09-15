import React from 'react';
import { render, act } from '@testing-library/react-native';
import { KustomCheckoutView } from '../KustomCheckoutView';
import RNKustomCheckoutView, {
  Commands,
} from '../specs/KustomCheckoutViewNativeComponent';

jest.mock('../specs/KustomCheckoutViewNativeComponent', () => {
  const MockReact = require('react');
  const NativeView = MockReact.forwardRef((_props: any, ref: any) => {
    MockReact.useImperativeHandle(ref, () => ({}));
    return null;
  });
  return {
    __esModule: true,
    default: NativeView,
    Commands: {
      setSnippet: jest.fn(),
      suspend: jest.fn(),
      resume: jest.fn(),
    },
  };
});

describe('KustomCheckoutView', () => {
  let viewRef: React.RefObject<KustomCheckoutView>;
  let onEvent: jest.Mock;
  let onError: jest.Mock;
  let getNativeProps: () => any;

  const triggerReady = () =>
    act(() => getNativeProps().onCheckoutViewReady({ nativeEvent: {} }));

  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(console, 'log').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});
    onEvent = jest.fn();
    onError = jest.fn();
    viewRef = React.createRef<KustomCheckoutView>();

    const { UNSAFE_getByType } = render(
      <KustomCheckoutView
        ref={viewRef}
        returnUrl="https://example.com"
        onEvent={onEvent}
        onError={onError}
      />
    );

    getNativeProps = () => UNSAFE_getByType(RNKustomCheckoutView as any).props;
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('setSnippet', () => {
    it('queues snippet and sends it exactly once once the view becomes ready', () => {
      viewRef.current!.setSnippet('snippet-a');
      expect(Commands.setSnippet).not.toHaveBeenCalled();

      triggerReady();

      expect(Commands.setSnippet).toHaveBeenCalledTimes(1);
      expect(Commands.setSnippet).toHaveBeenCalledWith(
        expect.anything(),
        'snippet-a'
      );
    });

    it('sends immediately when view is already ready', () => {
      triggerReady();

      viewRef.current!.setSnippet('snippet-b');

      expect(Commands.setSnippet).toHaveBeenCalledWith(
        expect.anything(),
        'snippet-b'
      );
    });

    it('overwrites queued snippet — only latest is sent', () => {
      viewRef.current!.setSnippet('snippet-a');
      viewRef.current!.setSnippet('snippet-b');
      triggerReady();

      expect(Commands.setSnippet).toHaveBeenCalledTimes(1);
      expect(Commands.setSnippet).toHaveBeenCalledWith(
        expect.anything(),
        'snippet-b'
      );
    });
  });

  describe('onEvent', () => {
    it('parses valid JSON params', () => {
      act(() => {
        getNativeProps().onEvent({
          nativeEvent: {
            productEvent: { action: 'purchase', params: '{"sku":"123"}' },
          },
        });
      });

      expect(onEvent).toHaveBeenCalledWith({
        action: 'purchase',
        params: { sku: '123' },
      });
    });

    it('falls back to empty object on invalid JSON params', () => {
      act(() => {
        getNativeProps().onEvent({
          nativeEvent: {
            productEvent: { action: 'purchase', params: 'not-valid-json' },
          },
        });
      });

      expect(onEvent).toHaveBeenCalledWith({ action: 'purchase', params: {} });
    });
  });

  describe('onError', () => {
    it('maps native error fields to KustomMobileSDKError', () => {
      act(() => {
        getNativeProps().onError({
          nativeEvent: {
            error: { isFatal: true, message: 'boom', name: 'NetworkError' },
          },
        });
      });

      expect(onError).toHaveBeenCalledWith({
        isFatal: true,
        message: 'boom',
        name: 'NetworkError',
      });
    });
  });

  describe('onResized', () => {
    it('updates height when value changes', () => {
      act(() => {
        getNativeProps().onResized({ nativeEvent: { height: '300' } });
      });

      expect(viewRef.current!.state.nativeViewHeight).toBe(300);
    });

    it('skips setState when height is unchanged', () => {
      act(() => {
        getNativeProps().onResized({ nativeEvent: { height: '300' } });
      });

      const spy = jest.spyOn(viewRef.current!, 'setState');

      act(() => {
        getNativeProps().onResized({ nativeEvent: { height: '300' } });
      });

      expect(spy).not.toHaveBeenCalled();
    });
  });

  describe('suspend/resume', () => {
    it('are no-op before view is ready', () => {
      viewRef.current!.suspend();
      viewRef.current!.resume();

      expect(Commands.suspend).not.toHaveBeenCalled();
      expect(Commands.resume).not.toHaveBeenCalled();
    });

    it('call native commands when ready', () => {
      triggerReady();
      viewRef.current!.suspend();
      viewRef.current!.resume();

      expect(Commands.suspend).toHaveBeenCalled();
      expect(Commands.resume).toHaveBeenCalled();
    });
  });
});
