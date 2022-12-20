import React from 'react';

class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            "base" : {},
            "base name" : "base.json",
            "bases list" : [],
            "log" : []
        };
        this.handleForward = this.handleForward.bind(this);
        this.handleBaseChange = this.handleBaseChange.bind(this);
    }

    componentWillMount() {
        const url = "/api/bases";

        fetch(url, {
            method : 'GET'
        })
        .then(response => response.json())
        .then(data => {
            this.setState(data);
        })
        .catch(error => console.log(error));
    }

    handleForward() {
        document.getElementById("forward-button").disabled = true;

        let request = {
            "base" : this.state["base name"],
            "memory" : []
        };

        this.state["base"]["variables"].forEach(variable => {
            const name = variable["name"];
            const select = document.getElementById(name + "-select");
            const value = select.options[select.selectedIndex].value;
            request["memory"].push({
                "variable" : name,
                "value" : value
            });
        });

        const url = "/api/bases";

        fetch(url, {
            method : 'POST',
            body : JSON.stringify(request)
        })
        .then(response => response.json())
        .then(data => {
            let new_base = this.state.base;
            new_base.memory = data.memory;
            this.setState({
                log : data.log,
                base : new_base
            }, () => {
                document.getElementById("forward-button").disabled = false;
            });
        })
        .catch(error => console.log(error));
    }

    handleBaseChange() {
        const select = document.getElementById("base-select");
        const selectedBase = select.options[select.selectedIndex].value
        if (selectedBase !== "") {
            const url = "/api/bases?base=" + selectedBase;

            fetch(url, {
                method : 'GET'
            })
            .then(response => response.json())
            .then(data => {
                data["base name"] = selectedBase;
                data["log"] = [];
                this.setState(data);
            })
            .catch(error => console.log(error));
        }
    }

    render() {
        return (
            <div className=" bg-secondary bg-opacity-10">
                <div className="page-header bg-dark py-2 ps-4">
                    <div className="d-inline-flex">
                        <img className="me-2" src="/icon.png" width="50" height="50" alt="" />
                        <h1 className="text-white">
                            Expert System
                        </h1>
                    </div>
                    <h4 className="text-muted">Current knowledge base - <em className="">{this.state["base name"]}</em> -</h4>
                </div>
                <div className="container">
                    <div className="row g-2">
                        <div className="col-md-7">
                            <div className="bg-secondary bg-opacity-25 rounded-3 p-2 my-2">
                                <KnowledgeBase rules={this.state.base["knowledge base"]} />
                            </div>
                            <div className="bg-secondary bg-opacity-25 rounded-3 p-2 my-2">
                                <Controls basesList={this.state["bases list"]}
                                    onForward={this.handleForward}
                                    onBaseChange={this.handleBaseChange} />
                            </div>
                        </div>
                        <div className="col-md">
                            <div className="bg-secondary bg-opacity-25 rounded-3 p-2 my-2">
                                <Variables variables={this.state.base["variables"]} memory={this.state.base["memory"]} /> 
                            </div>
                            <div className="bg-secondary bg-opacity-25 rounded-3 p-2 my-2">
                                <Log log={this.state.log} />
                            </div>
                        </div>
                    </div>
                </div>
                <div className="page-footer py-3 text-center">
                    Ayoub Hammal | Mohamed Ait Amara
                </div>
            </div>
        );
    }
}

class Controls extends React.Component {
    constructor(props) {
        super(props);
        this.handleForward = this.handleForward.bind(this);
        this.handleBaseChange = this.handleBaseChange.bind(this);
    }
    handleForward() {
        this.props.onForward();
    }

    handleBaseChange() {
        this.props.onBaseChange();
    }

    render() {
        if (this.props.basesList) {
            const options = this.props.basesList.map((base) => {
                return (
                    <option value={base}>{base}</option>
                );
            });
            return (
                <div>
                    <div>
                        <button className="btn btn-dark btn-sm btn-outline-light" id="forward-button" onClick={this.handleForward}>Forward Chaining</button>
                    </div>
                    <div className="form-floating">
                        <select className="form-select" id="base-select" onChange={this.handleBaseChange}>
                            <option value="" selected="selected">-- Knowledge Bases --</option>
                            {options}          
                        </select>
                        <label for="base-select">Select a knowledge base</label>
                    </div>
                </div>
            );
        } else {
            return (
                <div></div>
            );
        }
    }
}

class Log extends React.Component {
    render() {
        let log = null;
        let placeholder = (<tr><td colSpan={3}>No results yet</td></tr>);
        if (this.props.log.length > 0) {
            let stepId = 0;
            log = this.props.log.map(step => {
                stepId = stepId + 1;
                if (step["type"] === "target found") {
                    return (
                        <tr>
                            <td>{stepId}</td>
                            <td colSpan={2}>Target variable found</td>
                        </tr>
                    );
                } else if (step["type"] === "no rule") {
                    return (
                        <tr>
                            <td>{stepId}</td>
                            <td colSpan={2}>No rule can be applied</td>
                        </tr>
                    );
                } else if (step["type"] === "step") {
                    return (
                        <tr>
                            <td>{stepId}</td>
                            <td>{step["conflict set"]}</td>
                            <td>{step["selected rule"]}</td>
                        </tr>
                    );
                } else {
                    return (
                        <tr></tr>
                    );
                }
                
            });
        }
        return (
            <div className="py-3">
                <h2>Results</h2>
                <table className="table table-striped table-bordered table-dark table-responsive align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Step</th>
                            <th>Conflict Set</th>
                            <th>Selected Rule</th>
                        </tr>
                    </thead>
                    <tbody>
                        {this.props.log.length > 0 ? log : placeholder}
                    </tbody>
                </table>
            </div>
        );
    }
}

class Variables extends React.Component {
    render() {
        if (this.props.variables) {
            const memory = groupBy(this.props.memory, "variable", "value");

            const variablesInputs = this.props.variables.map((variable) => {
                const name = variable["name"];
                const values = variable["values"];
                return (
                    <div>
                        <VariableInput name={name} values={values} selected={memory[name]}/>
                    </div>
                );
            });
            return (
                <div>
                    <h2>Variables</h2>
                    <div>
                        {variablesInputs}
                    </div>
                </div>
            );
        } else {
            return (
                <div>
                </div>
            );
        }
    }
}

class VariableInput extends React.Component {
    render() {
        if (this.props.values) {
            const options = this.props.values.map((value) => {
                return (
                    <option value={value} selected={this.props.selected === value}>{value}</option>
                );
            });
            return (
                <div className="form-floating">
                    <select className="form-select" id={this.props.name + "-select"} aria-label="Floating label select example">
                        <option value=""></option>
                        {options}
                    </select>
                    <label for={this.props.name + "-select"}>{this.props.name}</label>
                </div>
            );
        } else {
            return (
                <fieldset>
                </fieldset>
            );
        }

    }
}

class KnowledgeBase extends React.Component {
    render() {
        let rules = null;
        if (this.props.rules) {
            rules = this.props.rules.map((rule) => {
                const label = rule["label"];
                const antecedents = rule["antecedents"].map((clause) => {
                    return Object.values(clause).join(" ");
                }).join(" AND ");
                const consequent = Object.values(rule["consequent"]).join(" ");

                return (
                    <tr>
                        <td>{label}</td>
                        <td>{antecedents}</td>
                        <td>{consequent}</td>
                    </tr>
                );
            });
        }
        return (
            <div>
                <h2>Knowledge Base</h2>
                <table className="table table-striped table-bordered table-dark table-responsive align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Label</th>
                            <th>Antecedents clauses</th>
                            <th>Consequent clause</th>
                        </tr>
                    </thead>
                    <tbody>
                        {this.props.rules ? rules : "No rules to display"}
                    </tbody>
                </table>
            </div>
        );
    }
}

function groupBy(list, key, value) {
    const map = {};
    list.forEach((item) => {
        const k = item[key];
        const v = item[value];
        map[k] = v;
    });
    return map;
}

export default App;
