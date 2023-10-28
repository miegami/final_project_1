import React from 'react';
import AppRoutes from './AppRoutes';
import {useSelector} from "react-redux";
import { useDispatch, useSelector} from "react-redux";
import { useEffect, useCallback } from "react";
import {loadUser } from "./redux-toolkit/messenger/asyncThunk";
import NotificationList from './components/Notification/NotificationList';

function App() {
  const { id } = useSelector(state => state.auth.token.obj) || null;
  const isAuth = !!id;

  const isHeader = !window.location.pathname.includes('/login') && !window.location.pathname.includes('/registration');



  const dispatch = useDispatch();
  const authUser = useSelector((state) => state.messenger.user.obj);
  useEffect(()=>{
    const id =localStorage.getItem("user")
    dispatch(loadUser({id}))
  }, [dispatch])

  const connectWebSocket = useCallback(() => {
    if (authUser.username) {
      dispatch({ type: 'webSocket/connect' });
    }
  }, [authUser, dispatch]);
  useEffect(() => {
    connectWebSocket();
  }, [connectWebSocket]);

  // useEffect(()=>{
  //   if(Object.keys(authUser).length>0){
  //     dispatch({ type: 'webSocket/connect'});
  //   }
  // },[authUser.username])
  if (isHeader) {
    return (
      <div className="App">
        <Header />
        <AppRoutes />
      </div>
    );
  }
  return (
    <div className="App">
      <AppRoutes />
      <NotificationList authUser={authUser}/>
      <AppRoutes isAuth={isAuth} />
    </div>
  );
}

export default App;
