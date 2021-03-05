import React from 'react';
import {pages, user_roles} from './App.js';
import './Payment.css';
import {loadStripe} from '@stripe/stripe-js';
import {Elements} from '@stripe/react-stripe-js';

//const stripePromise = window.Stripe('pk_live_51IRF2wCiDDET7BaUJFo3TLB61qWagmUezVnYNHVSXDjcFLvKrENsCHvmWmNgGdfl5Glb0ZWHo3VNLxYtrVIVbqhn00DAKpAp1N');
//const stripe = window.Stripe("pk_test_XXXXX");
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
      <div className="Payment">
        <button
          type="button"
          name="cash"
          value={this.state.cash}
          onClick={this.handlePaymentMethod}
          > Cash
        </button>
        <button
          type="button"
          name="credit_card"
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
          <CreditCard />
        )}
      </div>
    )}
}

class Cash extends React.Component{
  render(){
    let noCash = this.props.user !== null && this.props.user.hasOwnProperty('role')
                            && this.props.user.role !== user_roles.operator;
    return(
      <div className="Cash">
      {!noCash &&(
        <div>
          <p>Press "Finish Charging" button to terminate charging session.</p>
          <button
            type="button"
            name="Checkout"
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
      cash: true
    };
    this.handlePayment = this.handlePayment.bind(this);
  };

  handlePayment(e){
    /*const stripe = stripePromise;
    const res = stripe.redirectToCheckout({
      lineItems: [{
        price: 'price_1IRLiECiDDET7BaUxQ23fQ2o', // Replace with the ID of your price
        quantity: 1,
      }],
      mode: 'payment',
      successUrl: 'https://localhost:3000',
      cancelUrl: 'https://localhost:3000',
    });*/

}

  render(){
    return(
      <div className="CreditCard">
        <p>Credit Card Info</p>
        <button
          type="button"
          name="pay"
          onClick={this.handlePayment}
        > Pay
        </button>
      </div>
    );
  }
}

export default Payment;
