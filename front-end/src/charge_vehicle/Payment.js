import React from 'react';
import {user_roles} from '../app_essentials/App.js';
import {creditCardPayment} from '../api_comm/api.js';
import './Payment.css';
import Cards from 'react-credit-cards'
import 'react-credit-cards/es/styles-compiled.css'


class Payment extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      cash: true
    };
    this.handlePaymentMethod = this.handlePaymentMethod.bind(this);
  };

  handlePaymentMethod(e) {
    const new_state = e.target.name === "cash" ? true : false;
    this.setState({ cash: new_state});
  };

  render(){
    return (
      <div>
      {this.props.state.cost !== '0' &&(
        <div className="Payment">
        <button
          type="button"
          name="cash"
          class="btn_pay"
          value={this.state.cash}
          onClick={this.handlePaymentMethod}
          > Cash
        </button>
        <button
          type="button"
          name="credit_card"
          class="btn_pay"
          value={!this.state.cash}
          onClick={this.handlePaymentMethod}
        > Credit Card
        </button>
        {this.state.cash &&(
          <Cash
            user={this.props.user}
            handleCheckout={this.props.handleCheckout}/>
        )}
        {!this.state.cash &&(
          <CreditCard
            user={this.props.user}
            state={this.props.state}
            handleCheckout={this.props.handleCheckout}/>
        )}
      </div>
    )}
    {this.props.state.cost === '0' &&(
      <button
        type="button"
        name="Checkout"
        className="btn waves-effect waves-light"
        onClick={this.props.handleCheckout}
        > Finish Charging
      </button>
    )}
    </div>
    )}
}

class Cash extends React.Component{
  render(){
    let noCash = this.props.user !== null && this.props.user.hasOwnProperty('roles')
                            && !this.props.user.roles.includes(user_roles.operator);
    return(
      <div className="Cash">
      {!noCash &&(
        <div>
          <p>Press "Finish Charging" button to terminate charging session.</p>
          <button
            type="button"
            name="Checkout"
            className="btn waves-effect waves-light"
            onClick={this.props.handleCheckout}
            > Finish Charging
          </button>
        </div>
      )}
      {noCash &&(
        <h5>You must be an operator to accept payment in cash.</h5>
      )}
      </div>
    );
  }
}

class CreditCard extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      number: '',
      name: '',
      expiry: '',
      cvc: '',
      focus: '',
      issuer: '',
      error: ''
    };
    this.handleInput = this.handleInput.bind(this);
    this.handleFocus = this.handleFocus.bind(this);
    this.handleCallback = this.handleCallback.bind(this);
    this.handlePayment = this.handlePayment.bind(this);
  };

  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  handleFocus(e){
    this.setState({focus: e.target.name});
  }

  handleCallback({issuer}, isValid) {
    if(isValid) {
      this.setState({ issuer });
    }
  }

  handlePayment() {
    if (this.state.issuer !== ''){
      this.props.handleCheckout();
    }
  }

  render(){
    return(
      <div className="CreditCard">
        <Cards
          number={this.state.number}
          name={this.state.name}
          expiry={this.state.expiry}
          cvc={this.state.cvc}
          focused={this.state.focus}
          callback={this.handleCallback}
        />
        <input
          type="tel"
          name="number"
          placeholder="Card Number"
          value={this.state.number}
          onChange={this.handleInput}
          onFocus={this.handleFocus}
        />
        <input
          type="text"
          name="name"
          placeholder="Name"
          value={this.state.name}
          onChange={this.handleInput}
          onFocus={this.handleFocus}
        />
        <input
          type="text"
          name="expiry"
          placeholder="MM/YY Expiry"
          value={this.state.expiry}
          onChange={this.handleInput}
          onFocus={this.handleFocus}
        />
        <input
          type="tel"
          name="cvc"
          placeholder="CVC"
          value={this.state.cvc}
          onChange={this.handleInput}
          onFocus={this.handleFocus}
        />
        <button
          type="button"
          name="pay"
          className="btn waves-effect waves-light"
          role="link"
          onClick={this.handlePayment}
        > Pay
        </button>
        {this.state.error !== null && (
          <div className="error">
            <p>{this.state.error}</p>
          </div>
        )}
      </div>
    );
  }
}

export default Payment;
