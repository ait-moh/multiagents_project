import { useState, useEffect, useRef } from 'react';
import 'bootstrap/dist/css/bootstrap.css';
import { Modal } from 'bootstrap';

function App() {
    const [date, setDate] = useState({
        "month" : (new Date().getMonth() + 1).toString(),
        "day" : (new Date().getDate()).toString()
    });
    const [description, setDescription] = useState(null);
    const [items, setItems] = useState(null);
    const [modalShow, setModalShow] = useState(false);
    const [modalContent, setModalContent] = useState(null);
    const modalRef = useRef(null);

    useEffect(
        () => {
            const url = "/api/description";

            fetch(url, {
                method : 'GET'
            })
            .then(response => response.json())
            .then(data => {
                setDescription(data);
            })
            .catch(error => console.log(error));
        },
        []
    );

    function handleDateChange(newDate) {
        const today = new Date();
        if (newDate.month === "")
            newDate.month = (today.getMonth() + 1).toString();
        if (newDate.day === "")
            newDate.day = today.getDay().toString();
        setDate(newDate);
    }

    function handleFormValidation(formData, quantity) {
        const url = "/api/items";

        fetch(url, {
            method : 'POST',
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            data = { items : data };
            data["requested quantity"] = quantity;
            data["category"] = formData["category"];
            setItems(data);
        })
        .catch(error => console.log(error));
    }

    function handleItemPurchase(purchaseData) {
        const url = "/api/item/purchase";
        fetch(url, {
            method : 'POST',
            body: JSON.stringify(purchaseData)
        })
        .then(response => response.json())
        .then(data => {
            setModalContent(data);
            setModalShow(true);
            const myModal = Modal.getOrCreateInstance(modalRef.current);
            myModal.show();
        })
        .catch(error => console.log(error));
    }

    function handleModalClose() {
        setItems(null);
        setModalContent(null);
        setModalShow(false);
    }

    return (
        <div className="bg-secondary bg-opacity-10">
            <div className="d-md-flex justify-content-between page-header bg-dark py-2 px-4">
                <div className="d-inline-flex">
                    <img className="me-2" src="/icon.png" width="50" height="50" alt="" />
                    <h1 className="text-white">
                        The E-Shop
                    </h1>
                </div>
                <DateInput date={date} onDateChange={handleDateChange} />
            </div>
            <div className="container">
                <div className="row g-2">
                    <div className="col-md-3">
                        <div className="bg-secondary bg-opacity-25 rounded-3 p-2 my-2" style={{minHeight : "75vh"}}>
                            <SideBar
                                date={date}
                                description={description} 
                                onFormValidation={handleFormValidation}
                            />
                        </div>
                    </div>
                    <div className="col-md">
                        <div className="bg-secondary bg-opacity-25 rounded-3 p-2 my-2" style={{minHeight : "75vh"}}>
                            <Items 
                                date={date}
                                items={items} 
                                onItemPurchase={handleItemPurchase}
                            />
                        </div>
                    </div>
                </div>
                <PurchaseModal
                    modalRef={modalRef}
                    show={modalShow}
                    content={modalContent}
                    onModalClose={handleModalClose}
                />
            </div>
            <div className="page-footer py-3 text-center">
                Ayoub Hammal | Mohamed Ait Amara
            </div>
        </div>
    );
}

function DateInput(props) {
    function handleDateChange(e) {
        let newDate = {
            "month" : props.date.month,
            "day" : props.date.day
        }

        const input = e.target;
        newDate[input.name] = input.value;

        props.onDateChange(newDate);
    }

    return (
        <fieldset className="d-flex align-items-center">
            <legend className="text-white">Current Date</legend>
            <div className="d-flex flex-column mx-3">
                <label className="form-label text-white" for="month-input">Month</label>
                <input id="month-input" className="form-control-sm" type="text" name="month" 
                    placeholder={props.date.month} onChange={handleDateChange}/>
            </div>
            <div className="d-flex flex-column mx-3">
                <label className="form-label text-white" for="day-input">Day</label>
                <input id="day-input" className="form-control-sm" type="text" name="day" 
                    placeholder={props.date.day} onChange={handleDateChange} />
            </div>
        </fieldset>
    );
}

function SideBar(props) {
    const [category, setCategory] = useState("");

    const metaFeatures = ["id", "bundle", "month", "day"];

    function handleCategorySelection(e) {
        const select = e.target;
        const selectedCategory = select.options[select.selectedIndex].value;
        setCategory(selectedCategory);
    }

    function handleFormValidation(e) {
        e.preventDefault();
        let quantity = 1;

        const form = e.target;
        const inputs = Array.from(form.getElementsByTagName("input"));
        let data = {
            "category" : category,
            "filters" : [],
            "date" : props.date
        };

        inputs.forEach(input => {
            const feature = input.name;
            const value = input.value;
            const type = input.type
            if (value !== "" && type !== "submit") {
                if (feature === "max price") {
                    data.filters.push({
                        "feature" : "price",
                        "condition" : "<",
                        "value" : value
                    });
                } else {
                    data.filters.push({
                        "feature" : feature,
                        "condition" : "=",
                        "value" : value
                    });
                }
                if (feature === "quantity") {
                    quantity = value;
                }
            }
        });

        props.onFormValidation(data, quantity);
    }

    if (props.description) {
        const categoriesOptions = props.description.map((entry) => {
            return (
                <option value={entry.category}>{entry.category}</option>
            );
        });


        let form = null;
        if (category !== "") {
            let features = groupBy(props.description, "category", "features")[category];
            let inputs = features.map(feature => {
                if (! metaFeatures.includes(feature)) {
                    const id = feature + "-input";
                    if (feature === "price")
                        feature = "max price";
                    return (
                        <div className="form-floating mb-3">
                            <input id={id} className="form-control" type="text" name={feature} />
                            <label className="text-capitalize" for={id}>{feature}</label>
                        </div>
                    );
                } else {
                    return null;
                }
            });
            form = (
                <form onSubmit={handleFormValidation}>
                    {inputs}
                    <div>
                        <input className="btn btn-dark btn-sm btn-outline-light" type="submit" value="Search" />
                    </div>
                </form>
            );
        }
        return (
            <div>
                <div className="form-floating mb-3">
                    <select className="form-select" onChange={handleCategorySelection}>
                        <option id="category-select" value="" selected={true}>-- Select a category --</option>
                        {categoriesOptions}
                    </select>
                    <label for="category-select">Categories</label>
                </div>
                <div>
                    {form}
                </div>
            </div>
        );
    }
}

function Items(props) {
    if (props.items) {
        const category = props.items.category;
        const quantity = props.items["requested quantity"];
        const stores = props.items.items.sort((a, b) => a.seller > b.seller ? 1 : a.seller < b.seller ? -1 : 0).map(batch => {
            const seller = batch.seller;
            const name = batch.name;
            const promotions = batch.promotions;
            const items = batch.items;

            const promotionsStructured = promotions.map(promotion => {
                return (
                    <div>
                        <h3>{promotion.title} - {promotion.discount}%  <span className="badge bg-secondary">Promotion</span></h3>
                        <h4 className="text-muted">Starts on {promotion["start month"]}/{promotion["start day"]} and ends on {promotion["end month"]}/{promotion["end day"]}</h4>
                    </div>
                );
            });
            const itemsStructured = items.map(item => {
                return (
                    <Item 
                        date={props.date}
                        category={category}
                        quantity={quantity}
                        seller={seller}
                        item={item}
                        onItemPurchase={props.onItemPurchase}
                    />
                );
            });
            return (
                <div className="p-4">
                    <h1>{name}</h1>
                    {promotionsStructured}
                    <div>
                        {items.length > 0 ? itemsStructured : "No item to display"}
                    </div>
                </div>
            );
        });

        return (
            <div>
                {stores}
            </div>
        );
    } else {
        return (
            <div className="text-center">
                <h4>No items to display</h4>
            </div>
        );
    }
}

function Item(props) {
    const {bundle, ...features} = props.item;
    const bundleCheckbox = useRef(null);

    function handleItemPurchase(e) {
        const withBundle = bundle ? bundleCheckbox.current.checked : false;
        const purchaseData = {
            seller : props.seller,
            details : {
                category : props.category,
                id : features.id,
                quantity : props.quantity.toString(),
                bundle : withBundle.toString(),
                date : props.date
            }
        }
        props.onItemPurchase(purchaseData);
    }

    let bundleCard = null;
    if (bundle) {
        let bundleItem = bundle["item"];
        bundleCard = (
            <div className="form-check form-switch mb-2">
                <input ref={bundleCheckbox} id={props.seller + "-" + features.id + "-checkbox"} className="form-check-input" type="checkbox" value={false} />
                <label className="form-check-label" for={props.seller + "-" + features.id + "-checkbox"}> 
                    + {bundle.quantity} x {bundleItem.brand} {bundle.category} for {bundle.quantity * bundleItem.price} DA with - {bundle.discount} % discount <span className="badge bg-secondary">Bundle</span>
                </label>
            </div>
        );
    } else {
        bundleCard = (<div>No bundle available for this item</div>);
    }

    let metaFeatures = ["brand", "price", "total price", "id", "quantity"]
    let itemFeatures = Object.keys(features).map(key => {
        if (!metaFeatures.includes(key))
            return (
                <li className="list-group-item text-capitalize">{key}  :  {features[key]}</li>
            );
        return (
            <></>
        );
    });
    

    let itemCard = (
        <div className="card">
            <div className="card-body">
                <h5 className="card-title text-capitalize">Brand : {features["brand"]}</h5>
                <h6 className="card-subtitle mb-2 text-muted">Unit Price : {features["price"]} DA | Total Price : {features["total price"]} DA</h6>
                <div className="card-text mb-2">
                    Characteristics :
                    <ul className="list-group">
                        {itemFeatures}
                    </ul>
                </div>
                {bundleCard}
                <button className="btn btn-dark btn-sm btn-outline-light" onClick={handleItemPurchase}>Purchase</button>
            </div>
        </div>
    );

    return (
        <div>
            {itemCard}
        </div>
    );
}

function PurchaseModal(props) {
    let status = "";
    let message = "Nothing to display";
    if (props.content) {
        status = props.content.status === "completed" ? "Purchase Completed" : "Purchase failed";
        message = props.content.status === "completed" ? "Total purchase price is " + props.content["total price"] + " DA" : props.content.message;
    }
    return (
        <div ref={props.modalRef} className="modal" tabindex="-1">
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">{status}</h5>
                        <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close" onClick={props.onModalClose}></button>
                    </div>
                    <div className="modal-body">
                        <p>{message}</p>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" data-bs-dismiss="modal" onClick={props.onModalClose}>Close</button>
                    </div>
                </div>
            </div>
        </div>
    );
}

function groupBy(list, key, value) {
    const map = {};
    list.forEach((item) => {
        const k = item[key];
        let v = null;
        if (Array.isArray(value)) {
            v = {};
            value.forEach(item => {
                v[item] = list[item];
            });
        } else {
            v = item[value];
        }
        map[k] = v;
    });
    return map;
}
export default App;
