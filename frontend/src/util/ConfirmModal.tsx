import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from "reactstrap";

interface ConfirmModalProps {visible: boolean, setVisible: (visible: boolean) => void, onConfirm: () => void,
    title?: string, message?: string, confirmText?: string, cancelText?: string }

export default function ConfirmModal({ visible, setVisible, onConfirm, title = "Move salmons",
    message = "Continue and move salmons or undo moves?", confirmText = "Confirm", cancelText = "Cancel" }: Readonly<ConfirmModalProps>) {
    
        const toggle = () => setVisible(!visible);

    const closeBtn = (
        <button className="close" onClick={toggle} type="button"> &times; </button>
    );

    const handleConfirm = () => {
        onConfirm();
        setVisible(false);
    };

    return (
        <Modal isOpen={visible} toggle={toggle} keyboard={false}>
            <ModalHeader toggle={toggle} close={closeBtn}>
                {title}
            </ModalHeader>
            <ModalBody>
                {message}
            </ModalBody>
            <ModalFooter>
                <Button color="secondary" onClick={toggle}>
                    {cancelText}
                </Button>
                <Button color="primary" onClick={handleConfirm}>
                    {confirmText}
                </Button>
            </ModalFooter>
        </Modal>
    );
}