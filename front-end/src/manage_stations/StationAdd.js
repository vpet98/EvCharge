import React from 'react';
import './StationAdd.css';
import {addStation} from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

class StationAdd extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      info: { id: "",
              cost: "",
              address: "",
              country: "",
              lon: "",
              lat: "",
              points: ""},
      status: "",
      insert_mode: false,
      error: null
    };
    this.handleInput = this.handleInput.bind(this);
    this.handleAdd = this.handleAdd.bind(this);
    this.addStations = this.addStations.bind(this);
  }

  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    var new_info = this.state.info;
    new_info.[name] = value;
    this.setState({ info: new_info });
  }

  handleAdd(e){
    const value = e.target.name === "close" ? false : true;
    this.setState({insert_mode: value});
    this.setState({error: null});
  }

  addStations(){
    let obj = {
      info: this.state.info,
      token: this.props.user.token
    }
    addStation(obj)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response){
            this.setState({ status: "Ok" });
            this.setState({info: { id: "",
                                   cost: "",
                                   address: "",
                                   country: "",
                                   lon: "",
                                   lat: "",
                                   points: ""},})
            this.setState({error: null});
            this.props.handleInput();
          }
          else
            this.setState({ error: json.response.data.message });
        }, 0)
      })
      .catch(err =>{
        let handler = new AppiErrorHandler(err);
        let txt = handler.getMessage();
        if(txt !== null){
          if(handler.getError() !== null) txt = txt + '\n' + handler.getError();
        }else txt = handler.getError();
        this.setState({ error: txt });
      });
  }

  render(){
    return(
      <div className="AddStation">
        {!this.state.insert_mode && (
          <button
            type="button"
            name="AddStation"
            className="btn waves-effect waves-light add_btn"
            onClick={this.handleAdd}
            > +
          </button>
        )}
        {this.state.insert_mode &&(
          <div className="AddStationInfo">
            <GetInfo state={this.state} handleInput={this.handleInput}/>
            <button
              type="button"
              name="Add"
              className="btn waves-effect waves-light add_btn"
              onClick={this.addStations}
            > Add
            </button>
            <button
              type="button"
              name="close"
              className="btn waves-effect waves-light close_btn"
              onClick={this.handleAdd}
            > Close
            </button>
            {this.state.status &&(
              <p>Station successfully added!</p>
            )}
          </div>
        )}
        {this.state.error && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
      </div>
    );
  }
}

class GetInfo extends React.Component{

  render(){
    return(
      <div className="InsertInfo">
        <h5>Insert Station Info</h5>
        <input
          type="text"
          name="id"
          field="id"
          placeholder="station id"
          value={this.props.state.info.id}
          onChange={this.props.handleInput}
        />
        <input
          type="text"
          name="cost"
          field="cost"
          placeholder="cost"
          value={this.props.state.info.cost}
          onChange={this.props.handleInput}
        />
        <input
          type="text"
          name="address"
          field="address"
          placeholder="address"
          value={this.props.state.info.address}
          onChange={this.props.handleInput}
        />
        <input
          type="text"
          name="country"
          field="country"
          placeholder="country"
          value={this.props.state.info.country}
          onChange={this.props.handleInput}
        />
        <input
          type="text"
          name="lon"
          field="lon"
          placeholder="lon"
          value={this.props.state.info.lon}
          onChange={this.props.handleInput}
        />
        <input
          type="text"
          name="lat"
          field="lat"
          placeholder="lat"
          value={this.props.state.info.lat}
          onChange={this.props.handleInput}
        />
        <Point state={this.props.state} handleInput={this.props.handleInput}/>
      </div>
    );
  }
}

class Point extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      insert: false,
      point: ""
    };
    this.handlePoint = this.handlePoint.bind(this);
    this.handleInput = this.handleInput.bind(this);
    this.handleAdd = this.handleAdd.bind(this);
  }

  handleAdd(e){
    this.setState({insert: true});
  }

  handleInput(e){
    this.setState({point: e.target.value});
  }

  handlePoint(e){
    if(e.keyCode===13){
      const d = this.props.state.info.points === "" ? "" : ", ";
      const new_point = this.props.state.info.points+d+this.state.point;
      this.props.handleInput({target: {name: "points", value: new_point}});
    }
    this.props.handleInput({target: {name: "error", value: null}});
  }

  render(){
    return(
      <div className="Point">
        <button
          type="button"
          name="AddPoint"
          className="btn waves-effect waves-light add_btn"
          onClick={this.handleAdd}
        > +
        </button>
        {this.state.insert &&(
          <div>
          <p>Press enter to accept point.</p>
          <input
            type="text"
            name="point"
            field="point"
            placeholder="404_7.0_ac_type2"
            value={this.state.point}
            onChange={this.handleInput}
            onKeyUp={this.handlePoint}
          />
          <Point handleInput={this.props.handleInput} state={this.props.state}/>
          </div>
      )}
      </div>
    );
  }
}

export default StationAdd;
