import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from "reactstrap";

interface ErrorModalProps { readonly visible: boolean; readonly setVisible: (visible: boolean) => void; readonly message: string; }

export default function ErrorModal({ visible, setVisible, message }: Readonly<ErrorModalProps>) {
    if (!message) return null;

    const toggle = () => setVisible(!visible);

    const closeBtn = (
        <button className="close" onClick={toggle} type="button">
            &times;
        </button>
    );

    return (
        <Modal isOpen={visible} toggle={toggle} keyboard={false}>
            <ModalHeader toggle={toggle} close={closeBtn}>Alert!</ModalHeader>
            <ModalBody>
                {message}
            </ModalBody>
            <ModalFooter>
                <Button color="primary" onClick={toggle}>Close</Button>
            </ModalFooter>
        </Modal>
    );
}
