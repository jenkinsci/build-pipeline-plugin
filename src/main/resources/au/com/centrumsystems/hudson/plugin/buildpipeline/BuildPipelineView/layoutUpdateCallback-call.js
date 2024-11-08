// dropdownDescriptorSelectors will fill items up but don't update the layout, so need
// to do that explicitly
setTimeout(function(){
    layoutUpdateCallback.call();
}, 500);
