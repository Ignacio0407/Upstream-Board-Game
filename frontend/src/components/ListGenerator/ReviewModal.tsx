import { JSX } from "react";
import { Button } from "reactstrap";
import "./listGenerator.css";

interface ReviewModalProps<T> {originals: T[], updated: (T | undefined)[], renderItem: (original: T, updated?: T) => JSX.Element;
  onConfirm: () => void}

export function ReviewModal<T>({originals, updated, renderItem, onConfirm}: ReviewModalProps<T>) {
  return (
    <div className="mass-edit-modal">
      <h2>Review changes</h2>

      {originals.map((orig, i) => (
        <div key={i} className="review-block">
          {updated[i] ? (
            <>
              <div className="review-original">Old {renderItem(orig)}</div>
              <div className="review-divider"/>
              <div className="review-updated">New {renderItem(updated[i]!)}</div>
            </>
            )
          : <p><strong>Not updated</strong></p>}
        </div>
      ))}

      <Button color="success" onClick={onConfirm}> Confirm all </Button>
    </div>
  );
}